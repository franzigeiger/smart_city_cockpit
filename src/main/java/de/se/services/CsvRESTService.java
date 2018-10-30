package de.se.services;

import de.se.model.CsvBuilder;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Path("")
public class CsvRESTService {
    private static Logger logger = Logger.getLogger(CsvRESTService.class);

    /**
     * Provides a REST endpoint for downloading a CSV file with a list of all vehicles and notifications from the database
     * It builds a CSV file from the CSV string provided by the CsvBuilder
     * @return a Response containing the CSV file
     * @throws Exception if something goes really wrong
     */
    @GET
    @Path("/csv")
    @Produces("text/csv")
    public Response getCSV() throws Exception {
        CsvBuilder csvBuilder = new CsvBuilder();

        File file = new File("Vehicles and Notifications.csv");
        OutputStream stream = new FileOutputStream(file);
        stream.write(csvBuilder.buildCSV().getBytes());

        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=Vehicles and Notifications.csv");

        logger.info("Sending CSV file response on REST path /csv ...");
        return response.build();
    }
}
