package de.se.model.interfaces;

import de.se.data.*;
import de.se.model.ParentService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Network extends ParentService, HasState {

    /**
     * Gets all lines from persistence layer
     *
     * @return a list of all lines
     */
    List<Line> getLines();

    /**
     * Gets all stops from the persistence layer
     *
     * @return a list of all stops
     */
    List<Stop> getStops();

    /**
     * This method returns all actual and planned tours for a line
     *
     * @param line
     * @return
     */
    List<Tour> getTourSchedule(String line);

    /**
     * Get actual driving line vehicles for a line
     *
     * @param line
     * @return
     */
    List<Vehicle> getLineVehicles(String line);

    /**
     * This method removes a notification from the system.
     *
     * @param stopIDs           the stop ids on which the notification should be deleted.
     *                          If the list contains all stops of a line the notification will be deleted on a line
     * @param deleteDescription the text of the notification to be deleted
     * @throws Exception
     */
    boolean removeNotification(List<String> stopIDs, String lineID, String deleteDescription) throws Exception;

    /**
     * This method return all stops of a line
     *
     * @param line
     * @return
     */
    List<Stop> getStopsPerLine(String line);

    /**
     * This method return a list of all notifications on this line.
     * The notifications are either for a whole line or for one stop.
     * Also the stop notifications are contained here.
     *
     * @param lineID
     * @return
     */
    List<Notification> getNotifications(String lineID);

    /**
     * This method adds a notification to a list of stop ids
     *
     * @param stopIDs
     * @param note
     */
    void addStopNotification(List<String> stopIDs, String lineID, String note);

    /**
     * Updates this.stops with newly added problems
     * This method is called from the live engine. After that, the Network is responsible for
     * the consistency of the stops data
     *
     * @param problemsToAdd a hash map from the stop ID identifying a stop to a list of stop problems
     */
    void updateStops(HashMap<String, List<TimedStopProblem>> problemsToAdd);

    /**
     * Updates this.lines with newly added problems
     * This method is called from the live engine. After that, the Network is responsible for
     * the consistency of the lines data
     *
     * @param problemsToAdd a hash map from the line ID identifying a line to a list of line problems
     */
    void updateLines(HashMap<String, List<TimedLineProblem>> problemsToAdd);

    /**
     * This method return a line object
     *
     * @param targetLine
     * @return
     */
    public Line getLine(String targetLine);

    /**
     * this method removes a problem for the given id and timestamp
     *
     * @param stopOrLineId
     * @param time
     * @throws Exception
     */
    void removeProblem(String stopOrLineId, Date time) throws Exception;

    /**
     * Return a stop for given id.
     *
     * @param id
     * @return
     */
    Stop getStopPerID(String id);

    /**
     * This method returns a list of tuples, containing the time of line tours
     * at which a vehicle will be at a ceratin stop.
     *
     * @param stopId The stop to get the timetable for.
     * @return A list of times for lines at the given stop
     */
    List<Map.Entry<Long, String>> getStopTimeTable(String stopId) throws Exception;

    /**
     * This method allows to set a stop to with the given id to closed or open (true and false)
     *
     * @param stopId the id of the stop we want to change the closed status
     * @param isClosed true when the stop should be closed and false when it should be open
     */
    void setStopToClosedOrOpenedById(String stopId, boolean isClosed);

    /**
     * This method returns whether a stop is closed or not
     *
     * @param stopId the id to identify the stop for which we want the closed information
     * @return whether the stop with the given id is closed or not
     * @throws Exception when there is no stop with the id it throws an exception
     */
    boolean isStopClosedById(String stopId) throws Exception;

    /**
     *
     * @return all Ids of stops which are closed at the moment
     */
    List<String> getAllClosedStopIds();
}
