package de.se.DB;

import de.se.DB.hibernate_models.Timebetweenstopsdb;

import java.util.List;

public interface TimeBetweenStopsPersistence {
    /**
     * Fetches Timebetweenstopsdb from the database
     * @return a list of all Timebetweenstopsdb objects from the database
     */
    List<Timebetweenstopsdb> fetchTimeBetweenStops();

    /**
     * RETURNS -1 when connection does not exists. Order of the parameters does not matter
     * @param firstStopID stop in the stopsdb were a connection should exisits to ...
     * @param secondStopID ...the second stop in the stopsdb
     * @return time in minutes between the given stops if a direct connection exists
     */
    int getTimeBetweenTwoConnectedStops(String firstStopID, String secondStopID);

    /**
     * Saves a Timebetweenstopsdb object to the database.
     * @param timebetweenstopsdb the Timebetweenstopsdb object to be saved in the database
     */
    void saveTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb);

    /**
     * Deletes the Timebetweenstopsdb object from the database
     * @param timebetweenstopsdb object which will be deleted
     */
    void deleteTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb);
}
