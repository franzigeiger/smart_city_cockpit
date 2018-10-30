package de.se.services;

import de.se.data.Feedback;
import de.se.data.enums.FeedbackEnum;
import de.se.data.enums.VehicleProblem;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import de.se.model.interfaces.FleetService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class FeedbackRESTServiceTest {
    @BeforeClass
    public static void initialize(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        ServiceRegistry.getService(FleetService.class);
    }

    @Test
    public void getFeedbacks() throws ParseException, IOException {

            FeedbackRESTService service= new FeedbackRESTService();
            String json = service.getFeedbacks();

            JSONParser parser = new JSONParser();
            JSONArray feedbacksJson = (JSONArray)parser.parse(json);

           checkJson(feedbacksJson);
    }

    private void checkJson(JSONArray feedbacksJson){
        for (Object fdbObject : feedbacksJson) {
            JSONObject feedback =(JSONObject)fdbObject;

            assertFalse( feedback.get("id" ).equals(""));
            assertFalse( feedback.get("type" ).equals(""));
            assertTrue(feedback.get("type").equals(FeedbackEnum.General.name()) ||
                    feedback.get("type").equals(FeedbackEnum.Stop.name()) ||
                    feedback.get("type").equals(FeedbackEnum.Line.name()) ||
                    feedback.get("type").equals(FeedbackEnum.Vehicle.name()));
            if(!feedback.get("type").equals(FeedbackEnum.General.name())){
                assertFalse( feedback.get("type" ).equals(""));
            }

            assertFalse( feedback.get("description" ).equals(""));
            assertFalse( feedback.get("reason" ).equals(""));
            assertFalse( feedback.get("timestamp" ).equals(""));
            assertFalse( feedback.get("finished" ).equals(""));

        }
    }

    @Test
    public void getUnfinished() throws ParseException, IOException {

        FeedbackRESTService service= new FeedbackRESTService();
        String json = service.getUnfinishedFeedbacks();

        JSONParser parser = new JSONParser();
        JSONArray feedbacksJson = (JSONArray)parser.parse(json);

        checkJson(feedbacksJson);
    }

    /**
     * THis method mustn't be tested, because we don't want to change database content. The service method is tested.
     * And in rest endpoint mehtod no logic is executed.
     * @throws ParseException
     */
    @Test
    public void testSetFeedbackDone() throws ParseException {
            FeedbackService service =ServiceRegistry.getService(FeedbackService.class);
            Feedback fdb= service.getAllFeedbacks().get(0);
            FeedbackRESTService rest= new FeedbackRESTService();
            Response response = rest.setFeedbackDone(fdb.getId());
            assertEquals(response.getStatus(), 200);

    }

}
