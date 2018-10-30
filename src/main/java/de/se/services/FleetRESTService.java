package de.se.services;



import de.se.data.*;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("")
public class FleetRESTService {

    Logger logger = Logger.getLogger(FleetRESTService.class);

    /**
     * Builds a JSON object from the vehicles from the FleetService
     * @return a JSON string containing all vehicles with attributes vehicleID, type, deleted and vehicleProblems
     * @throws IOException in case something goes wrong when constructing the JSON object
     */
    @GET
    @Path("/fleet")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFleet() throws IOException {
        try {
            FleetService fleet = ServiceRegistry.getService(FleetService.class);
            List<Vehicle> vehicles = fleet.getAllVehicles();
            JSONArray fleetArray = new JSONArray();

            for (Vehicle vehicle : vehicles) {
                JSONObject vehicleJson = new JSONObject();
                vehicleJson.put("vehicleID", vehicle.getId());
                vehicleJson.put("type", vehicle.getType());
                vehicleJson.put("deleted", vehicle.isDeleted());

                fleetArray.add(vehicleJson);
            }

            return fleetArray.toJSONString();
        } catch(Exception e) {
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }


    /**
     * This method removes a problem from a vehicle pased on the vehicles id and the problems timestamp.
     * @param vehicleId
     * @param time
     * @return
     * @throws ParseException
     */
    @POST
    @Path("/fleet/deleteProblem/{vehicleId}/{timestamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeProblem(@PathParam("vehicleId") String vehicleId, @PathParam("timestamp") long time) throws ParseException {
        try {
            FleetService fleet =ServiceRegistry.getService(FleetService.class);

            fleet.removeVehicleProblem(vehicleId, new Date(time));
            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    /**
     * This  method adds a new vehicle to system based on the input parameter from json.
     * @param json
     * @return
     * @throws ParseException
     */
    @POST
    @Path("/fleet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveVehicle(String json) throws ParseException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject vehicleInput = (JSONObject)parser.parse(json) ;

            String id =(String)vehicleInput.get("name" );
            String type = (String)vehicleInput.get("type");

            Vehicle vehicleToSave =new Vehicle(id, type , null ,null, new ArrayList<TimedVehicleProblem>(),null , 0);

            FleetService fleet = ServiceRegistry.getService(FleetService.class);

            fleet.saveVehicle(vehicleToSave);

            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }


    /**
     * This method sets the deleted flag in a vehicle. The vehicle is not deleted from system.
     * @param vehicleId
     * @return
     */
    @DELETE
    @Path("/fleet/{vehicleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteVehicle(@PathParam("vehicleId") String vehicleId){
        try{
            FleetService fleet =ServiceRegistry.getService(FleetService.class);
            fleet.removeVehicle(vehicleId);
            return Response.ok().build();
        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    /**
     * This method return the information about a vehicle
     * @param vehicle
     * @return
     * @throws IOException
     */
    @GET
    @Path("/fleet/vehicle/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVehicle(@PathParam("vehicleId") String vehicle)  {
        try{
        FleetService fleet = ServiceRegistry.getService(FleetService.class);

        Vehicle fullVehicle =fleet.getVehicleForId(vehicle);

        List<String> lines = fleet.getLinesForVehicle(fullVehicle);

        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);
        List<Feedback> feedbacks =feedbackService.getFeedbacksForID(vehicle);

        ServiceRequestService service = ServiceRegistry.getService(ServiceRequestService.class);

        List<ServiceRequest> requests = new ArrayList<>();
        try{
          requests=service.getServicesForRefID(vehicle);
        }catch(Exception e){
            //we catch the exception to avoid error message at getting line infos just because SAP is driving crazy again.
            logger.error("Fetching SAP Service Requests had an error: " + e.getMessage());
            e.printStackTrace();
        }

        JSONObject vehicleJson = new JSONObject();
        vehicleJson.put("name", fullVehicle.getId());
        vehicleJson.put("type" , fullVehicle.getType());
        JSONArray lineJson = new JSONArray();
        for(String line : lines){
            lineJson.add(line);
        }

        vehicleJson.put("lines", lineJson);

        JSONArray fdbsJson = new JSONArray();

        for(Feedback fdb : feedbacks){
            JSONObject fdbJson = new JSONObject();
            fdbJson.put("id" , fdb.getId());
            fdbJson.put("description" , fdb.getContent() );
            fdbJson.put("reason", fdb.getReason());
            fdbJson.put("timestamp" , fdb.getTimeStamp().getTime());
            fdbJson.put("finished" , fdb.getFinished());
            fdbsJson.add(fdbJson);
        }

        vehicleJson.put("feedbacks", fdbsJson);

        JSONArray requestsJson = new JSONArray();
        for(ServiceRequest request : requests){
            JSONObject requestJson = new JSONObject();
            requestJson.put("id",request.getId());
            requestJson.put("creation", request.getCreationDate().getTime());
            requestJson.put("name", request.getName());
            requestJson.put("dueDate", request.getRequestedEnd().getTime());
            requestJson.put("description", request.getServiceRequestDescription());
            requestJson.put("objectType", "Mock");
            requestJson.put("priority", request.getServicePriorityCode());
            requestJson.put("objectId", request.getReferenceID());
            requestJson.put("feedback", request.getFeedbackReferenceID());

            requestJson.put("state", request.getLifeCycleCodeString());

            String serviceType="" ;
            switch(request.getProcessingTypeCode()){
                case SRRQ: serviceType = "Maintenance";
                    break;
                case ZCLN: serviceType = "Cleaning";
            }

            requestJson.put("serviceType", serviceType);

            requestsJson.add(requestJson);

        }

        vehicleJson.put("services" , requestsJson);

        return vehicleJson.toJSONString();
    }catch(Exception e){
        logger.error(e.getMessage() );
        JSONObject error = new JSONObject();
        error.put("error","An error occured: " + e.getMessage());
        return error.toJSONString();
    }
    }

    /*
    This method return the shift plan(a list of tour for one vehicle)
    for the given vehicle.
     */
    @GET
    @Path("/fleet/shiftplan/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getShiftPlan(@PathParam("vehicleId") String vehicle){
        try{
        FleetService fleet = ServiceRegistry.getService(FleetService.class);
        List<Tour> tours =fleet.getShiftPlan(vehicle);
        JSONArray shiftPlan = new JSONArray();

        Network network =ServiceRegistry.getService(Network.class);

        for(Tour tour : tours){
            JSONObject tourJson = new JSONObject();
            Line line = network.getLine(tour.getLine());
            tourJson.put("tourId", tour.getId());
            tourJson.put("lineId" , tour.getLine());
            tourJson.put("name" , line.getName());
            tourJson.put("startTime" , tour.getStartTime().getTime());
            tourJson.put("endTime" , tour.getEndTime().getTime());
            shiftPlan.add(tourJson);
        }


        return shiftPlan.toJSONString();
    }catch(Exception e){
        logger.error(e.getMessage() );
        JSONObject error = new JSONObject();
        error.put("error","An error occured: " + e.getMessage());
        return error.toJSONString();
    }
    }

    /*
    This mehtod return the state of the fleet, containing the states of all vehicles in fleet.
     */
    @GET
    @Path("/fleet/state")
    @Produces(MediaType.APPLICATION_JSON)
    public String getState(){
        try{
        FleetService fleet = ServiceRegistry.getService(FleetService.class);

        State state = fleet.getState();

        List<State> vehicleStates = state.getSubstates();

        JSONArray vehicles= new JSONArray();
        for(State vehicleState: vehicleStates){
            JSONObject vehicle = new JSONObject();
            vehicle.put("id" , vehicleState.getProblem().getElemID());
            vehicle.put("type" , vehicleState.getProblem().getProblemType());
            vehicle.put("state" , vehicleState.getType().name());

            vehicle.put("problems" , generateProblemArray(vehicleState.getProblem().getProblem()));

            vehicles.add(vehicle);
        }

        return vehicles.toJSONString();
        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }

    /*
    This method generates a json array out of the problem objects.
     */
    public JSONArray generateProblemArray(List<? extends TimedProblem> problems){
        JSONArray problemsJson = new JSONArray();
        for(TimedProblem problem : problems){
            JSONObject problemJson = new JSONObject();

            problemJson.put("description",problem.getGeneralProblem().getProblemDescription());
            problemJson.put("timestamp" , problem.getDate().getTime());
            problemJson.put("severity" , problem.getSeverity());

            problemsJson.add(problemJson);
        }

        return problemsJson;
    }



}
