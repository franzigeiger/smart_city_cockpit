package de.se.services;

import de.se.data.enums.VehicleEnum;
import de.se.data.enums.VehicleProblem;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FleetRESTServiceTest {

    @BeforeClass
    public static void initialize() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        ServiceRegistry.getService(FleetService.class);
    }

    @Test
    public void testGetFleet() throws Exception {
        FleetRESTService fleetRESTService = new FleetRESTService();

        String fleetJSONString = fleetRESTService.getFleet();
        assertTrue(!fleetJSONString.contains("error"));

        JSONParser parser = new JSONParser();
        JSONArray fleetJsonArray = (JSONArray) parser.parse(fleetJSONString);

        for (Object object : fleetJsonArray) {
            JSONObject vehicleJson = (JSONObject) object;
            assertFalse(vehicleJson.isEmpty());

            assertFalse(vehicleJson.get("vehicleID").equals(""));
            assertFalse(vehicleJson.get("deleted").equals(""));
            assertTrue(vehicleJson.get("type").equals(VehicleEnum.Bus.toString()) ||
                    vehicleJson.get("type").equals(VehicleEnum.Tube.toString()));


        }
    }

    @Test
    public void testGetShiftPlan() throws IOException, ParseException {
        FleetRESTService service = new FleetRESTService();
        String plan = service.getShiftPlan("Vehicle_0");
        JSONParser parser = new JSONParser();
        JSONArray shiftPlan = (JSONArray) parser.parse(plan);

        assertTrue(shiftPlan.size() > 0);

        for (int i = 0; i < shiftPlan.size(); i++) {
            JSONObject planLine = (JSONObject) shiftPlan.get(i);
            assertFalse(planLine.get("tourId").equals(""));
            assertFalse(planLine.get("lineId").equals(""));
            assertFalse(planLine.get("name").equals(""));
            assertFalse(planLine.get("startTime").equals(""));
            assertFalse(planLine.get("endTime").equals(""));
        }
    }

    @Test
    public void testGetVehicle() throws IOException, ParseException {
        FleetRESTService service = new FleetRESTService();
        String vehicle = service.getVehicle("");
        JSONParser parser = new JSONParser();
        JSONObject vehicleJson = (JSONObject) parser.parse(vehicle);

        assertFalse(vehicleJson.get("name").equals(""));
        assertFalse(vehicleJson.get("type").equals(""));
        assertFalse(vehicleJson.get("lines").equals(""));
        assertFalse(vehicleJson.get("feedbacks").equals(""));
        assertFalse(vehicleJson.get("services").equals(""));

        JSONArray lines = (JSONArray) vehicleJson.get("lines");
        for (int i = 0; i < lines.size(); i++) {
            assertFalse(lines.get(i).equals(""));
        }

        JSONArray feedbacks = (JSONArray) vehicleJson.get("feedbacks");
        for (int i = 0; i < feedbacks.size(); i++) {
            JSONObject fdb = (JSONObject) feedbacks.get(i);
            assertFalse(fdb.get("id").equals(""));
            assertFalse(fdb.get("description").equals(""));
            assertFalse(fdb.get("timestamp").equals(""));
            assertFalse(fdb.get("finished").equals(""));
        }
        JSONArray services = (JSONArray) vehicleJson.get("services");
        for (int i = 0; i < services.size(); i++) {
            JSONObject request = (JSONObject) services.get(i);

            assertFalse(request.get("id").equals(""));
            assertFalse(request.get("creation").equals(""));
            assertFalse(request.get("name").equals(""));
            assertFalse(request.get("dueDate").equals(""));
            assertFalse(request.get("description").equals(""));
            assertFalse(request.get("objectType").equals(""));
            assertFalse(request.get("priority").equals(""));
            assertFalse(request.get("objectId").equals(""));
            assertFalse(request.get("feedback").equals(""));
            assertFalse(request.get("serviceType").equals(""));
            assertTrue(request.get("serviceType").equals("Maintenance") || request.get("serviceType").equals("Cleaning"));
        }


    }

    @Test
    public void testSaveAndRemoveVehicle() throws ParseException {
        FleetRESTService service = new FleetRESTService();
        assertEquals(service.saveVehicle("{\"name\": \"test-vehicle-franzi\", \"type\": \"train\"}").getStatus(), 200);

        assertEquals(service.deleteVehicle("test-vehicle-franzi").getStatus(), 200);
    }

    @Test
    public void removeProblem() throws ParseException {
        FleetRESTService service = new FleetRESTService();
        assertEquals(service.removeProblem("V1", new Date().getTime()).getStatus(), 200);
    }

    @Test
    public void testGetFleetState() throws Exception {
        FleetRESTService service = new FleetRESTService();
        String stateJson = service.getState();

        JSONParser parser = new JSONParser();
        JSONArray vehiclesJSon = (JSONArray) parser.parse(stateJson);

        assertNotNull(vehiclesJSon);

        for (Object problem : vehiclesJSon) {
            JSONObject vehicleJson = (JSONObject) problem;

            assertFalse(vehicleJson.get("id").equals(""));
            assertFalse(vehicleJson.get("type").equals(""));
            assertFalse(vehicleJson.get("state").equals(""));
            assertNotNull(vehicleJson.get("problems").equals(""));

            JSONArray problemsJson = (JSONArray) vehicleJson.get("problems");

            for (Object object : problemsJson) {
                JSONObject problemJson = (JSONObject) object;
                assertFalse(problemJson.get("description").equals(""));
                assertFalse(problemJson.get("timestamp").equals(""));
                assertFalse(problemJson.get("severity").equals(""));

                assertTrue(problemJson.get("description").equals(VehicleProblem.Accident.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Defect_Engine.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Dirty.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Defect_Air_Conditioner.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Defect_Door.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Defect_Wheel.toString()) ||
                        problemJson.get("description").equals(VehicleProblem.Delay.toString()));


            }
        }
    }

}