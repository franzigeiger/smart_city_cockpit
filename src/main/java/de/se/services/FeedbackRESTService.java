package de.se.services;

import de.se.data.Feedback;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("")
public class FeedbackRESTService {

    Logger logger = Logger.getLogger(FeedbackRESTService.class);


    @GET
    @Path("/feedbacks")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFeedbacks() throws IOException {
        return buildFeedbacksJSONString(false);
    }

    @GET
    @Path("/unfinishedFeedbacks")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUnfinishedFeedbacks() throws IOException {
        return buildFeedbacksJSONString(true);
    }

    /**
     * This method does the actual work of building a JSON string. It is factored out because it is used almost
     * identically for the feedbacks and unfinishedFeedbacks. The only difference is whether getAllFeedbacks() or
     * getUnfinishedFeedbacks() is called on the feedback service.
     * @param retrieveOnlyUnfinishedFeedbacks a boolean indicating whether we should return all or only the unfinished feedbacks
     * @return a JSON string full of feedback data to send to the frontend
     */
    private String buildFeedbacksJSONString(boolean retrieveOnlyUnfinishedFeedbacks) {
        try {
            FeedbackService service= ServiceRegistry.getService(FeedbackService.class);
            List<Feedback> feedbacks = new ArrayList<>();
            if (retrieveOnlyUnfinishedFeedbacks) {
                feedbacks = service.getUnfinishedFeedbacks();
            } else {
                feedbacks = service.getAllFeedbacks();
            }
            JSONArray feedbackArray = new JSONArray();

            for (Feedback feedback : feedbacks) {
                JSONObject fdbJson = new JSONObject();
                fdbJson.put("id",feedback.getId());
                fdbJson.put("type",feedback.getType().name());
                fdbJson.put("targetId",feedback.getPlaceInstance());
                fdbJson.put("description",feedback.getContent());
                fdbJson.put("reason", feedback.getReason());
                fdbJson.put("timestamp",feedback.getTimeStamp().getTime());
                fdbJson.put("finished",feedback.getFinished());

                feedbackArray.add(fdbJson);
            }

            return feedbackArray.toJSONString();
        } catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }

    @POST
    @Path("/feedback/setFinished/{feedbackId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setFeedbackDone(@PathParam("feedbackId") int feedbackId) throws ParseException {
        try {
            FeedbackService service= ServiceRegistry.getService(FeedbackService.class);
            service.setFeedbackFinished(feedbackId);
            return Response.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

}
