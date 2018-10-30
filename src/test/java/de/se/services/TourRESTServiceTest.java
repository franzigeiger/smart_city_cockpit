package de.se.services;

import de.se.model.ServiceRegistry;
import de.se.model.interfaces.Network;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class TourRESTServiceTest {

    @BeforeClass
    public static void initialize() {
        //initaialize ServieRegistry
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        Network network = ServiceRegistry.getService(Network.class);
    }

    /**
     * This method test REST service method add a tour obect to backend.
     *
     * @throws IOException
     */
    @Test
    public void testAddTour() throws IOException {
        TourRESTService service = new TourRESTService();
        String requestBody = "{\"vehicleId\": \"Vehicle_0\",\n" +
                " \"startTimestamp\": 1234567,\n" +
                " \"lineId\" : \"l1\",\n" +
                " \"startStop\": \"one\",\n" +
                " \"endStop\": \"two\"}";
        Response resp = service.addTour(requestBody);
        assertEquals(200, resp.getStatus());

    }

    /**
     * This method test REST service method change the vehicle in one tour.
     *
     * @throws IOException
     */
    @Test
    public void testChangeVehicle() throws IOException {
        TourRESTService service = new TourRESTService();
        String requestBody = "{\"vehicleId\": \"V1\",\n" +
                " \"startTimestamp\": 1515417349,\n" +
                " \"lineId\" : \"l1\",\n" +
                " \"startStop\": \"one\",\n" +
                " \"endStop\": \"two\"}";
        Response resp = service.changeVehicle(1, requestBody);
        assertEquals(200, resp.getStatus());
    }

    @Test
    public void testRemoveTour() throws IOException {
        TourRESTService service = new TourRESTService();

        Response resp = service.removeTour(5);
        assertEquals(200, resp.getStatus());
    }

    /**
     * This method test REST service method to remove a vehicle from a tour.
     *
     * @throws IOException
     */
    @Test
    public void testremoveVehicleFromTour() throws IOException {
        TourRESTService service = new TourRESTService();

        Response resp = service.removeVehicleFromTour(1, "V2");
        assertEquals(200, resp.getStatus());

        String body = "{\"vehicleId\": \"V2\",\n" +
                " \"startTimestamp\": 1515417349,\n" +
                " \"lineId\" : \"l1\",\n" +
                " \"startStop\": \"one\",\n" +
                " \"endStop\": \"two\"}";

        assertEquals(200, service.changeVehicle(1, body).getStatus());
    }

    @Test
    public void testGetAvailableVehicles() throws IOException, ParseException {
        TourRESTService service = new TourRESTService();

        String resp = service.getAvailableVehicles("l1", 0);


        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(resp);
        assertEquals(json.size(), 4);

    }

    @Test
    public void testGetFreeTours() throws IOException, ParseException {
        TourRESTService service = new TourRESTService();

        String body = "{ \"vehicleId\": \"V1\",\n" +
                " \"rangestarttime\": 12344,\n" +
                "  \"rangeendtime\": 12345\n" +
                "}";

        String resp = service.getToursInTimeRangeForVehicle(body);


        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(resp);
        assertEquals(4, json.size());

        for (Object tour : json) {
            JSONObject tourJson = (JSONObject) tour;
            assertFalse(tourJson.get("tourId").equals(""));
            assertFalse(tourJson.get("lineId").equals(""));
            assertFalse(tourJson.get("name").equals(""));
            assertFalse(tourJson.get("startTime").equals(""));
            assertFalse(tourJson.get("endTime").equals(""));
            //   assertFalse( tourJson.get("vehicle" ).equals(""));
        }

    }

    @Test
    public void testGetPossibleReplacementVehicles() throws IOException, ParseException {
        TourRESTService service = new TourRESTService();

        String resp = service.getPossibleReplacementbusses("V1");

        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(resp);
        assertEquals(4, json.size());

        for (Object tour : json) {
            String vehicle = (String) tour;
            assertFalse(vehicle.equals(""));
        }

    }


    @Test
    public void testSetReplacementVehicle() throws IOException, ParseException {
        TourRESTService service = new TourRESTService();

        assertEquals(200, service.setReplacementBus("V1", "V2").getStatus());

    }


    @Test
    public void testVehicleLessTours() throws IOException, ParseException {
        TourRESTService service = new TourRESTService();

        String resp = service.getToursWithourVehicles();

        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(resp);
        assertEquals(4, json.size());

        for (Object tour : json) {
            JSONObject tourJson = (JSONObject) tour;
            assertFalse(tourJson.get("tourId").equals(""));
            assertFalse(tourJson.get("lineId").equals(""));
            assertFalse(tourJson.get("name").equals(""));
            assertFalse(tourJson.get("startTime").equals(""));
            assertFalse(tourJson.get("endTime").equals(""));
            //  assertFalse( tourJson.get("vehicle" ).equals(""));
        }

    }


}
