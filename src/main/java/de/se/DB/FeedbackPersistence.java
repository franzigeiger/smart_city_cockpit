package de.se.DB;

import de.se.data.Feedback;

import java.util.List;

/**
 * Fetches and deletes feedbacks from as well as saves feedbacks to database
 */
public interface FeedbackPersistence {
    /**
     * Fetches Feedbacks from the database
     * @return a list of all Feedback objects from the database
     */
    List<Feedback> fetchFeedbacks();

    /**
     * Saves a Feedback object to the database.
     * A mapping from Feedback to FeedbackDB has to be performed in this method
     * because the database only works with FeedbackDB objects and not Feedback objects.
     * @param feedback the Feedback object to be saved in the database
     */
    void saveFeedback(Feedback feedback);

    /**
     * Deletes a Feedback object from the database.
     * @param feedback the feedback object to delete from the database
     */
    void deleteFeedback(Feedback feedback);
}
