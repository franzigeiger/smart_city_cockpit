package de.se.services;

import de.se.model.ServiceRegistry;
import de.se.model.interfaces.LiveEngine;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class LiveEngineConfigRESTService {

    private Logger logger = Logger.getLogger(FleetRESTService.class);

    /**
     * This REST endpoint is used such that the backend can get the live engine configuration from the frontend
     * @return a JSON formatted string containing all relevant configuration information about the live engine
     */
    @GET
    @Path("/live-engine-config")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLiveEngineConfig() {
        try {
            LiveEngine liveEngine = ServiceRegistry.getService(LiveEngine.class);

            JSONObject configuration = new JSONObject();
            configuration.put("LIVE_ENGINE_FREQUENCY_SECONDS", liveEngine.getLIVE_ENGINE_FREQUENCY_SECONDS());
            configuration.put("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN());
            configuration.put("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN());
            configuration.put("LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE",
                    liveEngine.getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE());
            configuration.put("LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE",
                    liveEngine.getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE());
            configuration.put("LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE",
                    liveEngine.getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE());
            configuration.put("LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE",
                    liveEngine.getLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE());
            configuration.put("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN());
            configuration.put("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN",
                    liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN());
            configuration.put("LIVE_ENGINE_RUN_LIVE", liveEngine.isLIVE_ENGINE_RUN_LIVE());

            return configuration.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","The following error occurred while getting live engine configuration from backend: " + e.getMessage());
            return error.toJSONString();
        }
    }

    /**
     * This REST endpoint is used such that the frontend can transfer the live engine configuration to the backend
     * @param liveEngineConfigJson a JSON formatted string containing all relevant configuration information about the live engine
     */
    @POST
    @Path("/live-engine-config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setLiveEngineConfig(String liveEngineConfigJson) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject configurationJSON = (JSONObject) parser.parse(liveEngineConfigJson);

            LiveEngine liveEngine = ServiceRegistry.getService(LiveEngine.class);
            liveEngine.setLIVE_ENGINE_FREQUENCY_SECONDS((int) ((long) configurationJSON.get("LIVE_ENGINE_FREQUENCY_SECONDS")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE")));
            liveEngine.setLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE")));
            liveEngine.setLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE")));
            liveEngine.setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE")));
            liveEngine.setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(
                    (int) ((long) configurationJSON.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN")));
            liveEngine.setLIVE_ENGINE_RUN_LIVE((boolean) configurationJSON.get("LIVE_ENGINE_RUN_LIVE"));

            if (((boolean) configurationJSON.get("LIVE_ENGINE_SHOULD_RESTART"))) {
                liveEngine.restart();
            }

            return Response.ok().build();
        } catch (Exception e) {
            this.logger.warn("The following error occurred while setting live engine configuration from frontend: " + e.getMessage());
            return Response.status(Response.Status.CONFLICT.getStatusCode(),"The following error occurred while setting live engine configuration from frontend: " + e.getMessage() ).build();
        }
    }
}
