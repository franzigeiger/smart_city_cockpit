package de.se.services;

import de.se.data.Stop;
import de.se.data.TimedLineProblem;
import de.se.data.TimedStopProblem;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StopProblem;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.Network;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class NetworkRESTServiceTest {

    @BeforeClass
    public static void initialize() {
        //initaialize ServieRegistry
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        Network network = ServiceRegistry.getService(Network.class);

    }

    @Test
    public void testGetOverview() throws Exception {

        NetworkRESTService rest = new NetworkRESTService();
        String result = rest.getOverview();
        assertTrue(!result.contains("error"));
        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(result);

        for (Object object : json) {
            JSONObject lineJson = (JSONObject) object;
            assertTrue(!lineJson.get("lineID").equals(""));
            assertTrue(!lineJson.get("name").equals(""));
            assertTrue(!lineJson.get("color").equals(""));
            assertTrue(!lineJson.get("type").equals(""));

            JSONArray stopsJson = (JSONArray) lineJson.get("stops");
            assertNotNull(stopsJson);
            for (Object stop : stopsJson) {
                JSONObject stopJson = (JSONObject) stop;
                assertTrue(!stopJson.get("id").equals(""));
                assertTrue(!stopJson.get("name").equals(""));
                assertTrue(!stopJson.get("lat").equals(""));
                assertTrue(!stopJson.get("lon").equals(""));
            }


        }
    }

    @Test
    public void testGetLine() throws Exception {
        NetworkRESTService rest = new NetworkRESTService();
        String result = rest.getLine("l1");
        assertTrue(!result.contains("Error"));

        JSONParser parser = new JSONParser();
        JSONObject lineJson = (JSONObject) parser.parse(result);
        JSONArray vehicleJson = (JSONArray) lineJson.get("vehicles");
        for (Object vehicle : vehicleJson) {
            JSONObject vJson = (JSONObject) vehicle;
            assertTrue(!vJson.get("id").equals(""));
            assertTrue(!vJson.get("name").equals(""));
            assertTrue(!vJson.get("type").equals(""));
            assertTrue(!vJson.get("actual_position").equals(""));
            assertTrue(!vJson.get("planned_position").equals(""));

            JSONObject actualPositionJson = (JSONObject) vJson.get("actual_position");

            assertTrue(!actualPositionJson.get("prevStop").equals(""));
            assertTrue(!actualPositionJson.get("nextStop").equals(""));
            assertTrue(!actualPositionJson.get("isOnPrev").equals(""));

            JSONObject plannedPositionJson = (JSONObject) vJson.get("planned_position");

            assertTrue(!plannedPositionJson.get("prevStop").equals(""));
            assertTrue(!plannedPositionJson.get("nextStop").equals(""));
            assertTrue(!plannedPositionJson.get("isOnPrev").equals(""));


            //to do: add position!
            assertTrue(!vJson.get("finalStop").equals(""));

        }

        JSONArray noteJson = (JSONArray) lineJson.get("notifications");

        for (Object note : noteJson) {
            JSONObject noJs = (JSONObject) note;
            assertTrue(!noJs.get("id").equals(""));

            JSONArray stops = (JSONArray) noJs.get("stops");
            for (Object stop : stops) {
                String object = (String) stop;
                assertTrue(!object.equals(""));
            }

            assertTrue(!noJs.get("description").equals(""));
        }

        JSONArray feedbacksJson = (JSONArray) lineJson.get("feedbacks");

        for (Object feedback : feedbacksJson) {
            JSONObject feedbackJson = (JSONObject) feedback;

            assertTrue(!feedbackJson.get("id").equals(""));
            assertTrue(!feedbackJson.get("type").equals(""));
            assertTrue(!feedbackJson.get("targetId").equals(""));
            assertTrue(!feedbackJson.get("description").equals(""));
            assertTrue(!feedbackJson.get("reason").equals(""));
            assertTrue(!feedbackJson.get("timestamp").equals(""));
        }


        JSONArray servicesJson = (JSONArray) lineJson.get("services");

        for (Object service : servicesJson) {
            JSONObject requestJson = (JSONObject) service;

            assertFalse(requestJson.get("id").equals(""));
            assertFalse(requestJson.get("creation").equals(""));
            assertFalse(requestJson.get("name").equals(""));
            assertFalse(requestJson.get("dueDate").equals(""));
            assertFalse(requestJson.get("description").equals(""));
            assertFalse(requestJson.get("objectType").equals(""));
            assertTrue(requestJson.get("objectType").equals("Stop") || requestJson.get("objectType").equals("Vehicle"));
            assertFalse(requestJson.get("priority").equals(""));
            assertFalse(requestJson.get("objectId").equals(""));
            assertFalse(requestJson.get("feedback").equals(""));
            assertFalse(requestJson.get("serviceType").equals(""));
            assertTrue(requestJson.get("serviceType").equals("Maintenance") || requestJson.get("serviceType").equals("Cleaning"));
        }

    }

    @Test
    public void testGetClosedStops() {
        NetworkRESTService rest = new NetworkRESTService();
        String result = rest.getClosedStops();
        assertEquals("{\"stops\":[\"one\"]}", result);
    }

    @Test
    public void testToggleOpenAndCloseStop() {
        NetworkRESTService rest = new NetworkRESTService();
        Response resp = rest.toggleOpenAndCloseStop("one");
        assertEquals(200, resp.getStatus());
    }


    @Test
    public void testSaveNotification() {
        NetworkRESTService rest = new NetworkRESTService();
        String request = "{\n" +
                "\t\"stops\": [\"hammersmith-city\"],\n" +
                "\t\"description\" : \"test notification\"\n" +
                "}";

        String result = rest.saveNotification(request);
        assertTrue(result.contains("OK"));
    }

    @Test
    public void testRemoveNotification() {
        NetworkRESTService rest = new NetworkRESTService();
        String request = "{\n" +
                "\t\"stops\": [\"hammersmith-city\"],\n" +
                "\t\"description\" : \"test notification\"\n" +
                "}";

        String result = rest.saveNotification(request);
        assertTrue(result.contains("OK"));

        String removeRequest = "{\n" +
                "\t\"stops\": [\"hammersmith-city\"],\n" +
                "\t\"description\" : \"test notification\"\n" +
                "}";

        result = rest.deleteNotification(removeRequest);
        assertTrue(result.contains("OK"));
    }

    @Test
    public void testGetTimeTable() {
        try {
            NetworkRESTService rest = new NetworkRESTService();
            String result = rest.getTimeTables("l1");
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            JSONArray inbound = (JSONArray) json.get("inbound");
            JSONArray outbound = (JSONArray) json.get("outbound");
            assertNotNull(inbound);
            assertNotNull(outbound);

            for (int i = 0; i < inbound.size(); i++) {
                JSONObject tourObject = (JSONObject) inbound.get(i);
                assertNotNull(tourObject.get("vehicleId"));
                assertNotNull(tourObject.get("tourId"));
                assertNotNull(tourObject.get("stops"));

                JSONObject stops = (JSONObject) tourObject.get("stops");
                assertTrue(stops.size() > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    // The "normal" part can not be tested since the mock classes do not support it
    @Test
    public void testGetStopTimeTablesError() {
        NetworkRESTService rest = new NetworkRESTService();
        String result = rest.getStopTimeTables("--");
        assertEquals("{\"error\":\"An error occured: null\"}", result);
    }


    @Test
    public void testRemoveProblem() throws ParseException {
        List<TimedStopProblem> stopProblems1 = new ArrayList<>();
        stopProblems1.add(new TimedStopProblem(StopProblem.Dirty));

        List<TimedLineProblem> lineProblems2 = new ArrayList<>();
        lineProblems2.add(new TimedLineProblem(LineProblem.getRandomLineProblem()));

        Network network = ServiceRegistry.getService(Network.class);
        network.getLine("l1").setProblems(lineProblems2);
        Stop test = network.getStopsPerLine("l1").get(0);
        test.setProblems(stopProblems1);

        NetworkRESTService service = new NetworkRESTService();
        assertEquals(service.removeProblem("l1", lineProblems2.get(0).getDate().getTime()).getStatus(), 200);
        assertEquals(service.removeProblem(test.getId(), stopProblems1.get(0).getDate().getTime()).getStatus(), 200);
    }

    @Test
    public void testGetStateNetwork() throws Exception {
        NetworkRESTService service = new NetworkRESTService();
        String stateJson = service.getState();

        JSONParser parser = new JSONParser();
        JSONArray vehiclesJSon = (JSONArray) parser.parse(stateJson);

        assertNotNull(vehiclesJSon);

        for (Object problem : vehiclesJSon) {
            JSONObject lineJson = (JSONObject) problem;

            assertFalse(lineJson.get("id").equals(""));
            assertFalse(lineJson.get("type").equals(""));
            assertFalse(lineJson.get("state").equals(""));
            assertNotNull(lineJson.get("problems").equals(""));

            JSONArray problemsJson = (JSONArray) lineJson.get("problems");
            for (Object object : problemsJson) {
                JSONObject problemJson = (JSONObject) object;
                assertFalse(problemJson.get("description").equals(""));
                assertFalse(problemJson.get("timestamp").equals(""));
                assertFalse(problemJson.get("severity").equals(""));

                assertTrue(problemJson.get("description").equals(LineProblem.Obstacle.toString()) ||
                        problemJson.get("description").equals(LineProblem.General.toString()) ||
                        problemJson.get("description").equals(LineProblem.Overflow.toString()));


            }

            JSONArray stopJson = (JSONArray) lineJson.get("stops");
            for (Object object : stopJson) {
                JSONObject stop = (JSONObject) object;
                assertFalse(stop.get("id").equals(""));
                assertFalse(stop.get("type").equals(""));
                assertFalse(stop.get("state").equals(""));
                assertNotNull(stop.get("problems").equals(""));

                JSONArray stopProblemsJson = (JSONArray) stop.get("problems");
                for (Object stopObject : stopProblemsJson) {
                    JSONObject problemJson = (JSONObject) stopObject;
                    assertFalse(problemJson.get("description").equals(""));
                    assertFalse(problemJson.get("timestamp").equals(""));
                    assertFalse(problemJson.get("severity").equals(""));

                    assertTrue(problemJson.get("description").equals(StopProblem.Dirty.toString()) ||
                            problemJson.get("description").equals(StopProblem.Overflow.toString()));


                }

            }
        }
    }

}