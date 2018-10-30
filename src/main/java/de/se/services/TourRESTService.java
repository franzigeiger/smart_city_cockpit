package de.se.services;


import de.se.data.Line;
import de.se.data.Tour;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Path("")
public class TourRESTService {

    Logger logger = Logger.getLogger(TourRESTService.class);

    @POST
    @Path("/tours")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addTour(String tourJson) throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject tourInput = null;
        try {
            tourInput = (JSONObject)parser.parse(tourJson);

            String vehicleId =(String)tourInput.get("vehicleId");
            String lineID = (String) tourInput.get("lineId");
            long startTimeStamp = (long) tourInput.get("startTimestamp");
            String startStop = (String) tourInput.get("startStop");
            String endStop =(String) tourInput.get("endStop");

            Tour newTour = new Tour( -1 , new Timestamp(startTimeStamp), null,  startStop, endStop , vehicleId, lineID);

            ServiceRegistry.getService(TourStore.class).saveTour(newTour);

            return Response.ok(newTour.getId(), MediaType.TEXT_PLAIN).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @POST
    @Path("/tours/{tourId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response changeVehicle(@PathParam("tourId") int tourId, String tourJson) throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject tourInput = null;
        try {
            tourInput = (JSONObject)parser.parse(tourJson);

            String vehicleId =(String)tourInput.get("vehicleId");

            TourStore store =ServiceRegistry.getService(TourStore.class);
            Tour tour = store.getCopiedTourById(tourId);

            if(tour != null){
                store.updateTour(tour, vehicleId);
            } else {
                throw new Exception("Could not change the vehicle, the tour ID is not in system!");
            }

            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }


    @DELETE
    @Path("/tours/removeVehicle/{tourId}/{vehicleId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeVehicleFromTour(@PathParam("tourId") int tourId , @PathParam("vehicleId") String vehicleId) throws IOException {
        try {
            TourStore store =ServiceRegistry.getService(TourStore.class);
            Tour tour = store.getCopiedTourById(tourId);


            if(tour != null){
                store.updateTour(tour, null);
            } else {
                throw new Exception("Could not change the vehicle, the tour ID is not in system!");
            }

            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }


    @DELETE
    @Path("/tours/deleteTour/{tourId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeTour(@PathParam("tourId") int tourId) throws IOException {
        try {
            TourStore store =ServiceRegistry.getService(TourStore.class);
            Tour tour = store.getTourById(tourId);

            if(tour != null){
                store.removeTour(tour);
            } else {
                throw new Exception("Could not delete the tour, the tour ID is not in system!");
            }

            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT.getStatusCode(),e.getMessage()).build();
        }
    }

    @GET
    @Path("/availableVehicles/{lineID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getAvailableVehicles(@PathParam("lineID") String lineId, @QueryParam("startTime") long startTime) throws IOException {
        try{
        TourStore store =ServiceRegistry.getService(TourStore.class);
        List<String> vehicles =store.checkForAvailableVehicles(new Date(startTime), lineId);

        JSONArray vehiclesJson = new JSONArray();

        for(String v : vehicles){
            vehiclesJson.add(v);
        }
        return vehiclesJson.toJSONString();
        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }


    @POST
    @Path("tours/getFreeTours")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getToursInTimeRangeForVehicle(String json) {
        try{
        JSONParser parser = new JSONParser();
        JSONObject filterJson = (JSONObject)parser.parse(json) ;

        String vehicleId= (String)filterJson.get("vehicleId");
        long start =(long) filterJson.get("rangestarttime");
        long end =(long) filterJson.get("rangeendtime");

        TourStore store = ServiceRegistry.getService(TourStore.class);

        List<Tour> tours =store.getAssignableToursInTimeRange(vehicleId, start, end);

        JSONArray availableTours = new JSONArray();
        Network network =ServiceRegistry.getService(Network.class);

        for(Tour tour : tours){
            JSONObject tourJson = new JSONObject();
            Line line = network.getLine(tour.getLine());
            tourJson.put("tourId", tour.getId());
            tourJson.put("lineId" , tour.getLine());
            tourJson.put("name" , line.getName());
            tourJson.put("startTime" , tour.getStartTime().getTime());
            tourJson.put("endTime" , tour.getEndTime().getTime());
            tourJson.put("vehicle" , tour.getVehicle());
            availableTours.add(tourJson);
        }


        return availableTours.toJSONString();
        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }

    }

    @GET
    @Path("/getReplacementFor/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPossibleReplacementbusses(@PathParam("vehicleId") String vehicleId){
        try{
        TourStore store = ServiceRegistry.getService(TourStore.class);

        List<String> vehicles=store.getPossibleReplacementVehicles(vehicleId);

        JSONArray array = new JSONArray();

        for(String vehicle : vehicles){
            array.add(vehicle);
        }

        return array.toJSONString();

        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }

    @POST
    @Path("setReplacementFor/{oldVehicleId}/{newVehicleId}")
    public Response setReplacementBus(@PathParam("oldVehicleId") String old , @PathParam("newVehicleId") String newVehicle){
        try{
            TourStore store = ServiceRegistry.getService(TourStore.class);

            store.setReplacementVehicle(old, newVehicle);

            return Response.ok().build();

        }catch(Exception e){
            logger.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT.getStatusCode(), e.getMessage()).build();

        }


    }

    @GET
    @Path("/freeTours")
    @Produces(MediaType.APPLICATION_JSON)
    public String getToursWithourVehicles(){
        try{
            TourStore store = ServiceRegistry.getService(TourStore.class);
            Network network = ServiceRegistry.getService(Network.class);
            List<Tour> tours=store.getToursWithoutVehicles();

            JSONArray vehicleLessTours = new JSONArray();

            for(Tour tourItem: tours){
                JSONObject vehicleLessTour = new JSONObject();
                Line line = network.getLine(tourItem.getLine());
                vehicleLessTour.put("tourId", tourItem.getId());
                vehicleLessTour.put("lineId" , tourItem.getLine());
                vehicleLessTour.put("name" , line.getName());
                vehicleLessTour.put("startTime" , tourItem.getStartTime().getTime());
                vehicleLessTour.put("endTime" , tourItem.getEndTime().getTime());
                vehicleLessTours.add(vehicleLessTour);
            }

            return vehicleLessTours.toJSONString();

        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }





}
