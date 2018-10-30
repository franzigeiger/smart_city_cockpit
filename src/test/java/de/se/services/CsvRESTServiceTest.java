package de.se.services;

import de.se.model.ServiceRegistry;
import junit.framework.TestCase;

import javax.ws.rs.core.Response;

public class CsvRESTServiceTest extends TestCase {

    public void testGetCSV() throws Exception {
        ServiceRegistry.profile = "test";
        CsvRESTService csvRESTService = new CsvRESTService();
        Response response = csvRESTService.getCSV();
        assertEquals(200, response.getStatus()); //200 == "OK"
        assertEquals("attachment; filename=Vehicles and Notifications.csv",
                response.getHeaders().getFirst("Content-Disposition").toString());
    }

}