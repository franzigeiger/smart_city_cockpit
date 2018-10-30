package de.se.DB;

import de.se.DB.hibernate_models.Stopsinlinedb;

import java.util.List;

public interface StopsInLinePersistence {
    /**
     * Fetches StopsInLines from database
     * @return list of all StopsInLinesDBs from database
     */
    List<Stopsinlinedb> fetchStopsInLines();

    /**
     * @param lineId Id of the line which all returned stops should be on
     * @return a List of Stopsinlinedb sorted by position, all on the given ine
     */
    List<Stopsinlinedb> getSortedStopsonLine(String lineId);

    /**
     * Saves StopsInLineDB object to database
     * @param stopsInLine the StopsInLineDB object to be saved in the database
     */
    public void saveStopsInLine(Stopsinlinedb stopsInLine);
}
