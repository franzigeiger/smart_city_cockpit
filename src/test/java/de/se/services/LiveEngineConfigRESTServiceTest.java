package de.se.services;

import de.se.model.ServiceRegistry;
import de.se.model.interfaces.LiveEngine;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LiveEngineConfigRESTServiceTest {

    private LiveEngine liveEngine;
    private LiveEngineConfigRESTService liveEngineConfigRESTService;

    @Before
    public void init(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        this.liveEngine = ServiceRegistry.getService(LiveEngine.class);
        this.liveEngineConfigRESTService = new LiveEngineConfigRESTService();
    }

    @Test
    public void testGetLiveEngineConfig() throws Exception {
        String configString = this.liveEngineConfigRESTService.getLiveEngineConfig();
        JSONParser parser = new JSONParser();
        JSONObject configurationJSON = (JSONObject) parser.parse(configString);

        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_FREQUENCY_SECONDS(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_FREQUENCY_SECONDS")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(),
                (int) ((long) configurationJSON.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN")));
        Assert.assertEquals(this.liveEngine.isLIVE_ENGINE_RUN_LIVE(), configurationJSON.get("LIVE_ENGINE_RUN_LIVE"));
    }

    @Test
    public void testSetLiveEngineConfig() throws Exception {
        JSONObject configuration = new JSONObject();
        configuration.put("LIVE_ENGINE_FREQUENCY_SECONDS", 2);
        configuration.put("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN", 3);
        configuration.put("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN", 4);
        configuration.put("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN", 5);
        configuration.put("LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN", 6);
        configuration.put("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN", 7);
        configuration.put("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN", 8);
        configuration.put("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN", 9);
        configuration.put("LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE", 10);
        configuration.put("LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE", 11);
        configuration.put("LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE", 42);
        configuration.put("LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE", 50);
        configuration.put("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN", 60);
        configuration.put("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN", 70);
        configuration.put("LIVE_ENGINE_RUN_LIVE", true);

        this.liveEngineConfigRESTService.setLiveEngineConfig(configuration.toJSONString());

        Assert.assertEquals(configuration.get("LIVE_ENGINE_FREQUENCY_SECONDS"), this.liveEngine.getLIVE_ENGINE_FREQUENCY_SECONDS());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE"),
                this.liveEngine.getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE"),
                this.liveEngine.getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE"),
                this.liveEngine.getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE"),
                this.liveEngine.getLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN"),
                this.liveEngine.getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN());
        Assert.assertEquals(configuration.get("LIVE_ENGINE_RUN_LIVE"), this.liveEngine.isLIVE_ENGINE_RUN_LIVE());
    }

}