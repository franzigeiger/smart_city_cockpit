package de.se.services;


import de.se.data.*;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.ServiceRequestService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Path("")
public class NetworkRESTService {

    Logger logger = Logger.getLogger(NetworkRESTService.class);

    @GET
    @Path("/network")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOverview() throws IOException { // or return List<entity>
        try {
            Network network = ServiceRegistry.getService(Network.class);
            List<Line> lines = network.getLines();

            JSONArray linesJson = new JSONArray();
            for (Line line : lines) {
                JSONObject lineJson = new JSONObject();
                lineJson.put("lineID", line.getId());
                lineJson.put("name", line.getName());
                lineJson.put("color", line.getColor());
                lineJson.put("state", line.getState().getType().name());
                lineJson.put("type", line.getVehicleType().toString());

                JSONArray stopsJson = new JSONArray();
                for (Stop stop : line.getRoute().getStopSequence()) {
                    JSONObject stopJson = new JSONObject();
                    stopJson.put("id", stop.getId());
                    stopJson.put("name", stop.getName());
                    stopJson.put("lon", stop.getLongitude());
                    stopJson.put("lat", stop.getLatitude());
                    stopsJson.add(stopJson);
                }
                lineJson.put("stops", stopsJson);

                linesJson.add(lineJson);
            }
            return linesJson.toJSONString();

        } catch (Exception e) {
            logger.error(e.getMessage() + e.getStackTrace().toString());
            return "An error occured: " + e.getMessage();
        }
    }


    @GET
    @Path("/network/lines/{lineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLine(@PathParam("lineId") String lineId) throws IOException { // or return List<entity
        try {
            Network network = ServiceRegistry.getService(Network.class);
            List<Vehicle> vehicles = network.getLineVehicles(lineId);

            JSONObject lineJson = new JSONObject();
            JSONArray vehicleJson = new JSONArray();


            for (Vehicle vehicle : vehicles) {
                JSONObject vJson = new JSONObject();
                vJson.put("id", vehicle.getId());
                vJson.put("name", vehicle.getId());
                vJson.put("type", vehicle.getType());

                Position actualPosition = vehicle.getActualPosition();
                Position plannedPosition = vehicle.getPlannedPosition();
                JSONObject posJson = new JSONObject();
                if (actualPosition != null) {

                    posJson.put("prevStop", actualPosition.getPrevStop());
                    posJson.put("nextStop", actualPosition.getNextStop());
                    posJson.put("isOnPrev", actualPosition.isOnPrev());
                }

                vJson.put("actual_position", posJson);

                JSONObject planedPos = new JSONObject();
                if (plannedPosition != null) {

                    planedPos.put("prevStop", plannedPosition.getPrevStop());
                    planedPos.put("nextStop", plannedPosition.getNextStop());
                    planedPos.put("isOnPrev", plannedPosition.isOnPrev());
                }

                vJson.put("planned_position", planedPos);
                vJson.put("delayTime", vehicle.getDelayTimeInSeconds());
                vJson.put("finalStop", vehicle.getActualTour().getEndStop());
                vehicleJson.add(vJson);
            }

            lineJson.put("vehicles", vehicleJson);

            List<Notification> notes = network.getNotifications(lineId);
            JSONArray noteJson = new JSONArray();
            insertNotificationsToArray(notes, noteJson);
            lineJson.put("notifications", noteJson);


            FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);
            List<Feedback> feedbacks = feedbackService.getFeedbacksForID(lineId);

            for(Stop stop : network.getStopsPerLine(lineId) ){
                feedbacks.addAll(feedbackService.getFeedbacksForID(stop.getId()));
            }

            for(Vehicle vehicle : network.getLineVehicles(lineId) ){
                feedbacks.addAll(feedbackService.getFeedbacksForID(vehicle.getId()));
            }

            JSONArray feedbacksJson=new JSONArray();

            for (Feedback feedback : feedbacks) {
                if (!feedback.getFinished()) { //we only want to return finished feedbacks
                    JSONObject feedbackJson = new JSONObject();
                    feedbackJson.put("id", feedback.getId());
                    feedbackJson.put("type", feedback.getType().toString());
                    feedbackJson.put("targetId", feedback.getPlaceInstance());
                    feedbackJson.put("description", feedback.getContent());
                    feedbackJson.put("reason", feedback.getReason());
                    feedbackJson.put("timestamp", feedback.getTimeStamp().getTime());

                    feedbacksJson.add(feedbackJson);
                }
            }

            lineJson.put("feedbacks", feedbacksJson);

            List<ServiceRequest> requests = new ArrayList<>();
            try {
                ServiceRequestService service = ServiceRegistry.getService(ServiceRequestService.class);

                for (Vehicle vehicle : vehicles) {
                    requests.addAll(service.getServicesForRefID(vehicle.getId()));
                }

                for (Stop stop : network.getStopsPerLine(lineId)) {
                    requests.addAll(service.getServicesForRefID(stop.getId()));
                }
            }catch(Exception e){
                //we catch the exception to avoid error message at getting line infos just because SAP is driving crazy again.
                logger.error("Fetching SAP Service Requests had an error: " + e.getMessage());
                e.printStackTrace();
                }

            JSONArray serviceRequests = new JSONArray();
            for (ServiceRequest request : requests) {
                JSONObject requestJson = new JSONObject();
                requestJson.put("id", request.getId());
                requestJson.put("creation", request.getCreationDate().getTime());
                requestJson.put("name", request.getName());
                requestJson.put("dueDate", request.getRequestedEnd().getTime());
                requestJson.put("description", request.getServiceRequestDescription());
                requestJson.put("objectType", request.getReferenceType());
                requestJson.put("priority", request.getServicePriorityCode());
                requestJson.put("objectId", request.getReferenceID());
                requestJson.put("feedback", request.getFeedbackReferenceID());


                String serviceType = "";
                switch (request.getProcessingTypeCode()) {
                    case SRRQ:
                        serviceType = "Maintenance";
                        break;
                    case ZCLN:
                        serviceType = "Cleaning";
                }

                requestJson.put("state", request.getLifeCycleCodeString());
                requestJson.put("serviceType", serviceType);

                serviceRequests.add(requestJson);
            }


            lineJson.put("services", serviceRequests);

            return lineJson.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject error = new JSONObject();
            error.put("error","An error occured : " + e.getMessage());
            return error.toJSONString();
        }
    }

    private void insertNotificationsToArray(List<Notification> notes, JSONArray noteJson) {
        // 1. add all stop == null and sort the rest in a hash containing Notification-arrays
        // for returning equal descriptions in one object
        HashMap<String, List<Notification>> descriptionNotification = new HashMap<>();
        for (Notification note : notes) {
            // and notifications for whole line
            if (note.getTargetStop() == null) {
                JSONObject noJs = new JSONObject();
                noJs.put("id", note.getID());
                JSONArray stops = new JSONArray();
                for (Stop stop : note.getTargetLine().getRoute().getStopSequence()) {
                    stops.add(stop.getId());
                }
                noJs.put("stops", stops);
                noJs.put("description", note.getDescription());
                noteJson.add(noJs);
            }
            // hash the rest
            else {
                List<Notification> notificationsOfCurrentDescription = descriptionNotification.get(note.getDescription());
                if (notificationsOfCurrentDescription == null) {
                    notificationsOfCurrentDescription = new ArrayList<>();
                    notificationsOfCurrentDescription.add(note);
                    descriptionNotification.put(note.getDescription(), notificationsOfCurrentDescription);
                }
                else {
                    notificationsOfCurrentDescription.add(note);
                }
            }
        }

        // 2. Insert stops with the same description together
        for (String description : descriptionNotification.keySet()) {
            JSONObject noJs = new JSONObject();
            // only put the first id here because it is not used in the frontend
            noJs.put("id", descriptionNotification.get(description).get(0).getID());
            JSONArray stops = new JSONArray();
            for (Notification noti : descriptionNotification.get(description)) {
                stops.add(noti.getTargetStopID());
            }
            noJs.put("stops", stops);
            noJs.put("description", description);
            noteJson.add(noJs);
        }
    }

    @GET
    @Path("/network/linestest/{lineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLineJsonTest(@PathParam("lineId") String lineId) throws IOException { // or return List<entity>
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("line_test.json");

        return IOUtils.toString(is, "UTF-8");
    }

    @GET
    @Path("/stops/closed")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClosedStops() {
        Network network = ServiceRegistry.getService(Network.class);

        List<String> closedIds = network.getAllClosedStopIds();

        JSONObject closedIdsJson = new JSONObject();
        JSONArray closedIdsJsonArray = new JSONArray();
        closedIdsJson.put("stops", closedIdsJsonArray);

        for(String id : closedIds){
            closedIdsJsonArray.add(id);
        }

        return closedIdsJson.toJSONString();
    }

    @POST
    @Path("/stops/closed/{stopID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleOpenAndCloseStop(@PathParam("stopID") String stopId) {
        Network network = ServiceRegistry.getService(Network.class);

        Stop stop = network.getStopPerID(stopId);
        if (stop == null) {
            logger.warn("This stop does not exist and therefore can not change its closed status");
            return Response.status(Response.Status.CONFLICT).build();
        }
        stop.setClosed(!stop.isClosed()); // toggle between open and close

        return Response.ok().build();
    }

    @POST
    @Path("/notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String saveNotification(String note) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject notificationInput = (JSONObject) parser.parse(note);
            JSONArray stops = (JSONArray) notificationInput.get("stops");
            Network network = ServiceRegistry.getService(Network.class);

            String description = (String) notificationInput.get("description");
            String lineId = (String) notificationInput.get("lineId");

            List<String> stopIDs = new ArrayList<String>();
            for (int i = 0; i < stops.size(); i++) {
                stopIDs.add((String) stops.get(i));
            }

            network.addStopNotification(stopIDs, lineId, description);


            return "OK, saved successfully!";
        } catch (Exception e) {
            System.out.print("Problem occured!! Fontend go on!");
            return "Error occured: " + e.getMessage();
        }
    }

    @POST
    @Path("/notifications/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteNotification(String note) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject notificationInput = (JSONObject) parser.parse(note);
            JSONArray stops = (JSONArray) notificationInput.get("stops");
            Network network = ServiceRegistry.getService(Network.class);

            String description = (String) notificationInput.get("description");
            String lineId = (String) notificationInput.get("lineId");

            List<String> stopIDs = new ArrayList<String>();
            for (int i = 0; i < stops.size(); i++) {
                stopIDs.add((String) stops.get(i));
            }

            boolean wasSuccessful = network.removeNotification(stopIDs, lineId, description);


            if (wasSuccessful) {
                return "OK was successful!";
            } else {
                return "Error, could not delete all notifications";
            }
        } catch (Exception e) {
            System.out.print("Problem occured!! Fontend go on!");
            return "Error occured: " + e.getMessage();
        }
    }

    @GET
    @Path("/timetables/{lineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTimeTables(@PathParam("lineId") String lineId) { // or return List<entity>
        try {
            Network network = ServiceRegistry.getService(Network.class);

            List<Tour> tours = network.getTourSchedule(lineId);
            List<Stop> line = network.getStopsPerLine(lineId);

            JSONObject timeTableJson = new JSONObject();
            JSONArray inbound = new JSONArray();
            JSONArray outbound = new JSONArray();
            timeTableJson.put("inbound", inbound);
            timeTableJson.put("outbound", outbound);

            for (Tour tour : tours) {
                JSONObject tourJson = generateTourJson(tour);

                String beginning = tour.getStartStop();
                if (line.get(0).getId().equals(beginning)) {
                    inbound.add(tourJson);
                } else if (line.get(line.size() - 1).getId().equals(beginning)) {
                    outbound.add(tourJson);
                } else {
                    System.out.println("Line route start: " + line.get(0).getId() + " tour end " + line.get(line.size() - 1).getId() + " and tour beginning: " + tour.getStartStop() + " Route length: " + line.size());
                    throw new Exception("The tours beginning stop is not a start or end stop of the given line!!");
                }
            }

            return timeTableJson.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }


    @GET
    @Path("/stoptable/{stopId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStopTimeTables(@PathParam("stopId") String stopId) { // or return List<entity>
        try {
            Network network = ServiceRegistry.getService(Network.class);

            List<Map.Entry<Long, String>> stopTimes = network.getStopTimeTable(stopId);

            JSONArray times = new JSONArray();

            for (Map.Entry<Long, String> timeAtStop : stopTimes) {
                JSONObject time = new JSONObject();
                time.put("line", timeAtStop.getValue());
                time.put("time", timeAtStop.getKey());
                times.add(time);
            }

            return times.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }

    public JSONObject generateTourJson(Tour tour) {

        JSONObject tourJson = new JSONObject();
        tourJson.put("vehicleId", tour.getVehicle());
        tourJson.put("tourId", tour.getId());
        JSONObject stops = new JSONObject();

        for (Map.Entry<Stop, Date> stopTime : tour.getStopTime().entrySet()) {
            stops.put(stopTime.getKey().getId(), stopTime.getValue().getTime());
        }

        tourJson.put("stops", stops);

        return tourJson;

    }


    @POST
    @Path("/network/deleteProblem/{lineORStopId}/{timestamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeProblem(@PathParam("lineORStopId") String lineOrStop, @PathParam("timestamp") long time) throws ParseException {
        try {
            Network network = ServiceRegistry.getService(Network.class);

            network.removeProblem(lineOrStop, new Date(time));

            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @GET
    @Path("/network/state")
    @Produces(MediaType.APPLICATION_JSON)
    public String getState() throws Exception { // or return List<entity>
        try {
            Network network = ServiceRegistry.getService(Network.class);

            State state = network.getState();

            List<State> lineStates = state.getSubstates();

            JSONArray lines = new JSONArray();
            for (State lineState : lineStates) {
                JSONObject line = new JSONObject();
                line.put("id", lineState.getProblem().getElemID());
                line.put("type", lineState.getProblem().getProblemType());
                line.put("state", lineState.getType().name());

                line.put("problems", generateProblemArray(lineState.getProblem().getProblem()));

                JSONArray stops = new JSONArray();

                for (State stopState : lineState.getSubstates()) {
                    JSONObject stopJson = new JSONObject();
                    stopJson.put("id", stopState.getProblem().getElemID());
                    stopJson.put("type", stopState.getProblem().getProblemType());
                    stopJson.put("state", stopState.getType().name());

                    stopJson.put("problems", generateProblemArray(stopState.getProblem().getProblem()));

                    stops.add(stopJson);
                }

                line.put("stops", stops);

                lines.add(line);
            }

            return lines.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }

    public JSONArray generateProblemArray(List<? extends TimedProblem> problems) {
        JSONArray problemsJson = new JSONArray();
        for (TimedProblem problem : problems) {
            JSONObject problemJson = new JSONObject();

            problemJson.put("description", problem.getGeneralProblem().getProblemDescription());
            problemJson.put("timestamp", problem.getDate().getTime());
            problemJson.put("severity", problem.getSeverity());

            problemsJson.add(problemJson);
        }

        return problemsJson;
    }

}
