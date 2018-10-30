package de.se;

import de.se.data.Stop;
import de.se.data.TimedLineProblem;
import de.se.data.TimedStopProblem;
import de.se.data.Tour;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StopProblem;
import de.se.main.AppInitialization;
import de.se.model.ServiceRegistry;
import de.se.model.impl.FleetServiceImpl;
import de.se.model.interfaces.FeedbackService;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import de.se.services.*;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class UniversalMustRunTest {

    static Logger logger = Logger.getLogger(UniversalMustRunTest.class);

    @BeforeClass
    public static void startup(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile="run";
        logger.info("Setup test class");
        AppInitialization.start();
    }

    @Test
    public void executeNetworkRequest() throws Exception {
        NetworkRESTService service = new NetworkRESTService();
        assertTrue(service.getOverview().contains("lineID"));
    }

    @Test
    public void executeLineRequest() throws Exception {
        NetworkRESTService service = new NetworkRESTService();
        assertTrue(service.getLine("bakerloo").contains("vehicles"));
    }

    @Test
    public void executeAddNotification() throws Exception {
        NetworkRESTService service = new NetworkRESTService();
        assertTrue(service.saveNotification("{\n" +
                "\t\"stops\": [\"LUWWL\"],\n" +
                "\t\"description\" : \"test notification\"\n" +
                "\t\"lineId\" : \"victoria\"\n" +
                "}").contains("OK"));


        assertTrue(service.deleteNotification("{\n" +
                "\t\"stops\": [\"LUWWL\"],\n" +
                "\t\"description\" : \"test notification\"\n" +
                "\t\"lineId\" : \"victoria\"\n" +
                "}").contains("OK"));
    }

    @Test
    public void executeGetTimeTable() throws Exception {
        NetworkRESTService service = new NetworkRESTService();
        String json =service.getTimeTables("bakerloo");
        assertTrue(json.contains("outbound"));
    }

    @Test
    public void removeLineOrStopProblem() throws ParseException {
        List<TimedStopProblem> stopProblems1 = new ArrayList<>();
        stopProblems1.add(new TimedStopProblem(StopProblem.Dirty));

        List<TimedLineProblem> lineProblems2 = new ArrayList<>();
        lineProblems2.add(new TimedLineProblem(LineProblem.getRandomLineProblem()));

        Network network= ServiceRegistry.getService(Network.class);
        network.getLine("bakerloo").setProblems(lineProblems2);
        Stop test= network.getStopsPerLine("bakerloo").get(0);
        test.setProblems(stopProblems1);

        NetworkRESTService service = new NetworkRESTService();
        assertEquals(service.removeProblem("bakerloo", lineProblems2.get(0).getDate().getTime()).getStatus(), 200);
        assertEquals(service.removeProblem(test.getId(), stopProblems1.get(0).getDate().getTime()).getStatus(), 200);
    }


    /**
     * This method may not work any more at some time if there is a problem with the vehicle instances.
     * @throws IOException
     */
    @Ignore
    @Test
    public void changeVehicle() throws IOException {
        TourRESTService service = new TourRESTService();
         Response resp =service.changeVehicle(110 , "{\"vehicleId\": \"T_099\"}");

        assertTrue(resp.getStatus()==200);
    }

    /*

     */
    @Test
    public void removeAndAddTour()  {
        TourRESTService service = new TourRESTService();
        String body = "{\"vehicleId\": \"T_000\",\n" +
                " \"startTimestamp\": 0,\n" +
                " \"lineId\" : \"bakerloo\",\n" +
                " \"startStop\": \"LUHAW\",\n" +
                " \"endStop\": \"LUEAC\"}";
        try {
        Response resp = null;
        resp = service.addTour(body);

        int id=Integer.parseInt(resp.getEntity().toString());
        resp =service.removeTour(id);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getAvailableVehicles() throws IOException {
        TourRESTService service = new TourRESTService();
        String vehicles =service.getAvailableVehicles("bakerloo" , 0);
        assertTrue(vehicles.contains("T"));
    }

    @Test
    public void testGetFleet() throws IOException {
        FleetRESTService service = new FleetRESTService();
       assertTrue(service.getFleet().contains("vehicleID"));
    }

    @Test
    public void testSaveVehicle() throws ParseException {
        FleetRESTService service = new FleetRESTService();
        String timeid = "test-vehicle" +System.currentTimeMillis();
        assertEquals(service.saveVehicle("{\"name\": \""+timeid+"\", \"type\": \"tube\"}").getStatus(), 200);
        assertEquals(service.deleteVehicle(timeid).getStatus(), 200);
        //This is evil!!! Do never cast from interface to implementation class. This fails in case of another implementation >> Destroys the concept!!
        //I'm doing this here to delete the vehicle, which is officially not allowed!
        FleetService impl = ServiceRegistry.getService(FleetService.class);

        impl.deleteVehicleFromDb(timeid);
    }

    @Test
    public void testGetShiftPlan() throws IOException {
        FleetRESTService service = new FleetRESTService();
        assertTrue(service.getShiftPlan("T_007").contains("tourId"));
    }

    @Test
    public void testGetVehicle() throws IOException {
        FleetRESTService service = new FleetRESTService();
        service.getVehicle("T_7");
    }

    @Test
    public void removeProblem() throws ParseException {
        FleetRESTService service = new FleetRESTService();
        assertEquals(service.removeProblem("T_007", new Date().getTime()).getStatus(), 200);
    }

    @Test
    public void getFeebacks() throws IOException {
        FeedbackRESTService service =new FeedbackRESTService();
       assertTrue(service.getFeedbacks().contains("description"));
    }

    @Test
    public void getEvents() throws IOException {
        EventRESTService service = new EventRESTService();
        assertTrue(service.getEvents().contains("location"));
    }

    @Ignore
    public void getServiceRequests() throws IOException {
        ServiceRequestRESTService service = new ServiceRequestRESTService();
        assertTrue(service.getServiceRequests().contains("description"));
    }

    @Test
    public void getPossibleReplaceVehicles(){
        TourRESTService service = new TourRESTService();
        String vehicles =service.getPossibleReplacementbusses("T_010");

        logger.info(vehicles);
        assertTrue(vehicles.contains("T"));
    }

    @Test
    public void getFreeTours(){
        TourRESTService service = new TourRESTService();
        String vehicles =service.getToursWithourVehicles();
        // if there is no tour without vehicle at the moment...
        if(!vehicles.contains("name")) {
            // ...create one
            TourStore tourStore = ServiceRegistry.getService(TourStore.class);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            Timestamp timestampStart = new Timestamp(cal.getTimeInMillis());
            Tour tour = new Tour(-1, timestampStart, null, "LUEAC", "LUHAW", null, "bakerloo");
            try {
                tourStore.saveTour(tour);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("Could not added new tour without vehicle");
            }
        }

        // something in the implementation is wrong if there is still no tour without vehicle
        vehicles =service.getToursWithourVehicles();
        assertTrue(vehicles.contains("name"));
    }

}
