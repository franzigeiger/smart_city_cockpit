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
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ServiceRequestRESTServiceTest {
    @BeforeClass
    public static void initialize(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        ServiceRegistry.getService(FleetService.class);
    }

    @Test
    public void setAddFeedback() throws ParseException {
        ServiceRequestRESTService service= new ServiceRequestRESTService();

        String json = "{\"name\" : \"service8\",\n" +
                " \"referenceId\" : \"Vehicle_0\",\n" +
                "  \"type\": \"vehicle\", \n" +
                "  \"description\": \"test description\",\n" +
                "   \"serviceType\": \"Cleaning\",\n" +
                "  \"dueDate\": 1203491230,\n" +
                "  \"feedback\": [],\n" +
                "   \"priority\" : \"5\" \n" +
                "}";

        assertEquals(service.saveServiceRequest(json).getStatus(), 200);
    }

    @Test
    public void getAllServiceRequest() throws ParseException, IOException {
        ServiceRequestRESTService service= new ServiceRequestRESTService();
        String json =service.getServiceRequests();

        JSONParser parser = new JSONParser();
        JSONArray serviceRequests = (JSONArray) parser.parse(json);
        for(Object request : serviceRequests) {
            JSONObject requestJson = (JSONObject) request;

            assertFalse(requestJson.get("id").equals(""));
            assertFalse(requestJson.get("creation").equals(""));
            assertFalse(requestJson.get("name").equals(""));
            assertFalse(requestJson.get("dueDate").equals(""));
            assertFalse(requestJson.get("description").equals(""));
            assertFalse(requestJson.get("objectType").equals(""));
            assertFalse(requestJson.get("priority").equals(""));
            assertFalse(requestJson.get("objectId").equals(""));
            assertFalse(requestJson.get("feedback").equals(""));
            assertFalse(requestJson.get("serviceType").equals(""));
            assertTrue(requestJson.get("serviceType").equals("Maintenance") || requestJson.get("serviceType").equals("Cleaning"));
        }
    }
}
