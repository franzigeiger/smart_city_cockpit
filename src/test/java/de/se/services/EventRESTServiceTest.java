package de.se.services;

import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class EventRESTServiceTest {
    @BeforeClass
    public static void initialize(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        ServiceRegistry.getService(FleetService.class);
    }

    @Test
    public void testAddEvent() throws ParseException {
        EventRESTService service= new EventRESTService();

        String json = "{\"title\" : \"PArty hard\",\n" +
                " \"description\" : \"end of semester party0\",\n" +
                "  \"location\": \"uni augsburg\", \n" +
                "  \"involved\": \"all se students\",\n" +
                "   \"start\": 1516123699569,\n" +
                "  \"end\": 1516123799569\n" +
                "}";

        assertEquals(service.saveEvent(json).getStatus(), 200);
    }

    @Test
    public void testGetAllEvents() throws ParseException, IOException {
        EventRESTService service= new EventRESTService();
        String json =service.getEvents();

        JSONParser parser = new JSONParser();
        JSONArray eventsJson = (JSONArray) parser.parse(json);
        for(Object request : eventsJson) {
            JSONObject eventJson = (JSONObject) request;

            assertFalse(eventJson.get("id").equals(""));
            assertFalse(eventJson.get("start").equals(""));
            assertFalse(eventJson.get("end").equals(""));
            assertFalse(eventJson.get("location").equals(""));
            assertFalse(eventJson.get("description").equals(""));
            assertFalse(eventJson.get("title").equals(""));
            assertFalse(eventJson.get("involved").equals(""));
        }
    }
}
