package de.se.model.interfaces;

import de.se.data.Event;
import de.se.model.ParentService;

import java.util.List;

/**
 * Service class to get, save and remove events from the database
 */
public interface EventService extends ParentService {
    /**
     * Gets ALL events from the database
     * @return a list with all events that currently exist in the SAP database
     */
    List<Event> getAllEvents();

    /**
     * Saves the given event in the database
     * @param event the event to be saved in the SAP database
     * @return the HTTP response code for saving the event
     */
    int saveEvent(Event event);

    /**
     * Removes an event from the database
     * @param event the event to be removed from the SAP database
     * @return the HTTP response code for removing the event
     */
    int removeEvent(Event event);
}
