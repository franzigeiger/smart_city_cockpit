package de.se.DB;

import de.se.data.Line;

import java.util.List;

/**
 * Fetches lines from and saves lines to database
 */
public interface LinePersistence {
    /**
     * Fetches lines from the database
     * @return a list of all Line objects from database
     */
    List<Line> fetchLines();

    /**
     * Saves a Line object to the database.
     * A mapping from Line to LineDB has to be performed in this method
     * because the database only works with LineDB objects and not Line objects.
     * @param line the Line object to be saved in the database
     */
    void saveLine(Line line);
}
