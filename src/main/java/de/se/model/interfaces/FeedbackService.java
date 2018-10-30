package de.se.model.interfaces;

import de.se.data.Feedback;
import de.se.model.ParentService;

import java.sql.Timestamp;
import java.util.List;

/**
 * Service class to get, save and remove customer feedbacks from the database
 */
public interface FeedbackService extends ParentService {
    /**
     * Gets ALL feedbacks from the database
     * @return a list with all customer feedbacks that currently exist in the database
     */
    List<Feedback> getAllFeedbacks();

    /**
     * Gets all feedbacks for a given time period
     * @param start the lower time bound for the feedbacks that should be included in the result
     * @param end the upper time bound for the feedbacks that should be included in the result
     * @return a list of all customer feedbacks in the given time period
     */
    List<Feedback> getFeedbacksInTimePeriod(Timestamp start, Timestamp end);

    /**
     * Saves the given feedback in the database
     * @param feedback the feedback to be saved in the database
     */
    void saveFeedback(Feedback feedback);

    /**
     * Removes a feedback from the database
     * @param feedback the feedback to be removed from the database
     */
    void removeFeedback(Feedback feedback);

    /**
     * Computes a fresh feedback ID to avoid primary key violations
     * @return an unused feedback ID
     */
    int getFreshFeedbackID();

    /**
     * This method return a list of all feedbacks for the given instance id, either vehicle, line or stop.
     * @param id
     * @return
     */
    List<Feedback> getFeedbacksForID(String id);

    /**
     * This method changes the isFinished flag of the feedback with given id.
     * @param feedbackId the id of the feedback
     */
    void setFeedbackFinished(int feedbackId) throws Exception;

    /**
     * Gets all unfinished feedbacks from the database
     * @return a list with all unfinished customer feedbacks that currently exist in the database
     */
    List<Feedback> getUnfinishedFeedbacks();
}
