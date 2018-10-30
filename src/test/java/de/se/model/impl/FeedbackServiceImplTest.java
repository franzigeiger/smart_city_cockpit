package de.se.model.impl;

import de.se.data.Feedback;
import de.se.data.enums.FeedbackEnum;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.interfaces.FeedbackService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeedbackServiceImplTest {
    private static FeedbackService feedbackService = new FeedbackServiceImpl();

    @Before
    public void setUp() throws Exception {
        feedbackService.initialize();
    }

    @Test
    public void testSaveGetAndRemoveFeedback() throws Exception {
        Feedback feedback = DummyTestObjectGenerator.getDummyFeedback();

        int feedbacksSizeBeforeInserting = feedbackService.getAllFeedbacks().size();
        feedbackService.saveFeedback(feedback);

        int feedbacksSizeAfterInserting = feedbackService.getAllFeedbacks().size();
        assertEquals(feedbacksSizeBeforeInserting + 1, feedbacksSizeAfterInserting);

        feedbackService.removeFeedback(feedback);
        int feedbacksSizeAfterDeleting = feedbackService.getAllFeedbacks().size();
        assertEquals(feedbacksSizeAfterInserting - 1, feedbacksSizeAfterDeleting);
    }

    @Test
    public void testGetFeedbacksInTimePeriod() throws Exception {
        Date dateBeforeInsertingFeedback = new Date();
        Thread.sleep(100);
        Feedback feedback = DummyTestObjectGenerator.getDummyFeedback(); //gets a current feedback
        feedback.setId(1111); //to avoid duplicate keys
        feedbackService.saveFeedback(feedback);

        List<Feedback> feedbacksInTimePeriod = feedbackService.getFeedbacksInTimePeriod(
                        new Timestamp(dateBeforeInsertingFeedback.getTime()),
                        new Timestamp(new Date().getTime())); //feedbacks from before inserting the new feedback until now

        assertEquals(1, feedbacksInTimePeriod.size());
        assertEquals("T_055", feedbacksInTimePeriod.get(0).getPlaceInstance());
        assertEquals("We will delete this feedback right away", feedbacksInTimePeriod.get(0).getReason());

        feedbackService.removeFeedback(feedback);
    }

    @Test
    public void testSetFinishedFlag() throws Exception {
        int id= (int) System.currentTimeMillis();
        Feedback workFdb = new Feedback(id,"Content", new Date(), false, "Test feedback", FeedbackEnum.General , "");
        feedbackService.saveFeedback(workFdb);

        feedbackService.setFeedbackFinished(id);

        assertTrue(workFdb.getFinished());

        feedbackService.removeFeedback(workFdb);
    }

}