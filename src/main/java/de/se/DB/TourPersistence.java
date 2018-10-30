package de.se.DB;

import de.se.DB.hibernate_models.Tourdb;
import de.se.data.Tour;

import java.sql.Timestamp;
import java.util.List;

/**
 * Fetches and saves tours to database
 */
public interface TourPersistence {
    /**
     * Fetches tours from the database
     * @return a list of all Tour objects from the database
     */
    List<Tour> fetchTours();

    /**
     * Saves a Tour object to the database.
     * If the ID = -1 a new not used ID will be searched
     * A mapping from Tour to TourDB has to be performed in this method
     * because the database only works with TourDB objects and not Tour objects.
     * @param tour the Tour object to be saved in the database
     */
    void saveTour(Tour tour);

    /**
     * Save all tours to the database
     * @param tourdbs list of tourdb objects which should be saved
     */
    void saveTourdbs(List<Tourdb> tourdbs);

    /**
     * Deletes the TourDB object in tour from the database
     * @param tour object which contains the tourDB which will be deleted
     */
    void deleteTour(Tour tour);

    /**
     * Updates the tourdb object of the fiven tour in the database
     * @param tour contains the tourdb which should be updated
     */
    void updateTour(Tour tour);

    /**
     * Searches a tour with the given id
     * @param id the id of the tour we want to get
     * @return a tour with the givne id or null when not found
     */
    Tourdb getTourById(int id);

    /**
     * Deletes all toursdb objects which are older the given days
     * @param days when tourdb (start_time) ar older than 'days' days it will be deleted
     * @return return delete days
     */
    int deleteOlderToursThan(int days);

    /**
     * Returns the number of tours running on the given day ( starts and ends on the given day)
     * @param day timestamp which specifies the day of which we want the number of tours for
     *            The hours, minutes, seconds and milliseconds are not relevant
     * @return the number of tours running on the given day
     */
    int getNumberOfToursOnDay(Timestamp day);

    /**
     * Returns the maximum Id existing in the table
     * @return integer value with the max id
     */
    int getMaxID();
}
