package de.se.DB.impl;

import de.se.DB.FeedbackPersistence;
import de.se.DB.hibernate_models.Feedbackdb;
import de.se.DB.GeneralPersistence;
import de.se.data.Feedback;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class FeedbackPersistenceImpl extends GeneralPersistence implements FeedbackPersistence {
    @Override
    public List<Feedback> fetchFeedbacks() {
        final Session session = getNewSession();
        List<Feedbackdb> feedbacksDBs = session.createQuery("FROM Feedbackdb").list();

        List<Feedback> feedbacks = new ArrayList<>();
        for (Feedbackdb feedbackDB : feedbacksDBs) {
            feedbacks.add(new Feedback(feedbackDB));
        }

        session.close();
        return feedbacks;
    }

    @Override
    public void saveFeedback(Feedback feedback) {
        saveObjectToDatabase(feedback.getFeedbackdb());
    }

    @Override
    public void deleteFeedback(Feedback feedback) {
        deleteObjectFromDatabase(feedback.getFeedbackdb());
    }
}
