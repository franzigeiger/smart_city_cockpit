package de.se.DB.impl;

import de.se.DB.FeedbackPersistence;
import de.se.data.Feedback;
import de.se.data.Tour;
import de.se.model.DummyTestObjectGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FeedbackPersistenceImplTest {
    private FeedbackPersistence persistence = new FeedbackPersistenceImpl();

    /**
     * cleans the database when there is still a dummy object from failed test
     */
    @Before
    public void cleanDBBefore() {
        persistence.deleteFeedback(DummyTestObjectGenerator.getDummyFeedback());
    }

    @Test
    public void testFetchFeedbacks() throws Exception {
        assertNotNull(persistence.fetchFeedbacks());
    }
    @Test
    public void testPersistFetchAndDeleteFeedback() throws Exception {
        Feedback feedback = DummyTestObjectGenerator.getDummyFeedback();

        persistence.saveFeedback(feedback);
        List<Feedback> feedbacks = persistence.fetchFeedbacks();
        assertTrue(feedbackListecontainsFeedback(feedbacks, feedback));

        persistence.deleteFeedback(feedback);
        feedbacks = persistence.fetchFeedbacks();
        assertFalse(feedbackListecontainsFeedback(feedbacks, feedback));
    }

    private boolean feedbackListecontainsFeedback( List<Feedback> feedbacks, Feedback feedback) {
        for (Feedback feedb : feedbacks) {
            if (feedb.getId() == feedback.getId()) {
                return true;
            }
        }
        return false;
    }

}