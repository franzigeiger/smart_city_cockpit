package de.se.DB;

import de.se.data.Stop;

import java.util.List;

/**
 * Fetches and saves stops to database
 */
public interface StopPersistence {
    /**
     * Fetches stops from the database
     * @return a list of all Stop objects from the database
     */
    List<Stop> fetchStops();

    /**
     * Saves a Stop object to the database.
     * A mapping from Stop to StopDB has to be performed in this method
     * because the database only works with StopDB objects and not Stop objects.
     * @param stop the Stop object to be saved in the database
     */
    void saveStop(Stop stop);
}
