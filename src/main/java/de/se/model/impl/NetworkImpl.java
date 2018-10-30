package de.se.model.impl;

import de.se.DB.LinePersistence;
import de.se.DB.NotificationPersistence;
import de.se.DB.StopPersistence;
import de.se.DB.StopsInLinePersistence;
import de.se.DB.hibernate_models.Linedb;
import de.se.DB.hibernate_models.Stopdb;
import de.se.DB.hibernate_models.Stopsinlinedb;
import de.se.DB.impl.LinePersistenceImpl;
import de.se.DB.impl.NotificationPersistenceImpl;
import de.se.DB.impl.StopPersistenceImpl;
import de.se.DB.impl.StopsInLinePersistenceImpl;
import de.se.TransportAPI.TransportAPIHandler;
import de.se.data.*;
import de.se.data.enums.StateType;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.LiveEngine;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import org.apache.log4j.Logger;

import java.util.*;

public class NetworkImpl implements Network {

    private Logger logger = Logger.getLogger(Network.class);
    private Map<String, Stop> stops;
    private List<Line> lines;
    private List<Notification> notifications;

    private LinePersistence linePersistence;
    private StopPersistence stopPersistence;
    private NotificationPersistence notePersistence;
    private TransportAPIHandler handler;

    public NetworkImpl() {
        logger.info("Intanciate Network");
        linePersistence = new LinePersistenceImpl();
        stopPersistence = new StopPersistenceImpl();
        notePersistence = new NotificationPersistenceImpl();
    }

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public void initialize() {
        logger.info("Initialize Network");

        lines = linePersistence.fetchLines();
        stops = new HashMap<String, Stop>();

        addToMap(stopPersistence.fetchStops());

        handler = new TransportAPIHandler();
        initDBs();
        //to have unique stop objects we have to replace the db given stop objects with our real objects from stops map
        generateRouteForLines();

        notifications = notePersistence.fetchNotifications();
        injectNotifications();

        logger.info("Initialize Network done!");
    }

    private void injectNotifications() {
        for (Notification note : notifications) {
            if (note.getTargetLineID() == null || note.getTargetLineID().equals("")) {
                String id = note.getTargetStopID();
                note.setStop(stops.get(note.getTargetStopID()));
            } else {
                String id = note.getTargetLineID();
                note.setLine(getLineForID(note.getTargetLineID()));
            }
        }
    }

    /**
     * This method enriches the lines with the real stop objects
     */
    public void generateRouteForLines() {
        for (Line line : lines) {
            Route oldRoute = line.getRoute();
            List<Stop> fullStops = new ArrayList<Stop>();
            for (Stop oldStop : oldRoute.getStopSequence()) {
                fullStops.add(stops.get(oldStop.getId()));
            }
            Route newRoute = new Route(fullStops);
            line.setRoute(newRoute);
        }
    }

    public void addToMap(List<Stop> newStops) {
        for (Stop stop : newStops) {
            stops.put(stop.getId(), stop);
        }
    }


    /**
     * Fills all databases when they are not already filled
     */
    public void initDBs() {
        if (lines.size() == 0) {
            saveAllLines();
            lines = linePersistence.fetchLines();
        }

        if (stops.size() == 0) {
            saveAllStops();
            addToMap(stopPersistence.fetchStops());
        }

        StopsInLinePersistenceImpl stopsInLinePersistence = new StopsInLinePersistenceImpl();
        if (stopsInLinePersistence.fetchStopsInLines().size() == 0) {
            saveAllStopsInLine(stopsInLinePersistence);
            //fetch lines again so they have the right route
            lines = linePersistence.fetchLines();
        }
    }

    /**
     * Helpermethod for creating all stopsInLinedb objects in the database
     *
     * @param stopsInLinePersistence
     */
    private void saveAllStopsInLine(StopsInLinePersistenceImpl stopsInLinePersistence) {
        List<Stopsinlinedb> stopsinlinedbs = handler.fetchStopsInLineDBs();
        for (Stopsinlinedb stopsinlinedb : stopsinlinedbs) {
            stopsInLinePersistence.saveStopsInLine(stopsinlinedb);
        }
    }

    /**
     * Saves all lines to database
     */
    public void saveAllLines() {
        List<Linedb> lineDBs = handler.fetchLineDBs();
        for (Linedb linedb : lineDBs) {
            Line line = new Line(linedb);
            linePersistence.saveLine(line);
        }
    }


    public void saveAllStops() {
        List<Stopdb> stopDBs = handler.fetchStopDBs();
        for (Stopdb stopdb : stopDBs) {
            Stop stop = new Stop(stopdb);
            stopPersistence.saveStop(stop);
        }
    }


    /**
     * Return all line objects which contains their stops
     *
     * @return
     */
    @Override
    public List<Line> getLines() {
        //logger.info("Get all existing Lines"); //this gives way too much log output
        if (lines == null) { //to avoid NullPointerException
            return new ArrayList<>();
        }
        LiveEngine engine = ServiceRegistry.getService(LiveEngine.class);
        engine.enrichLines(lines);

        for (Line line : lines) {
            engine.enrichStops(line.getRoute().getStopSequence());
        }

        return lines;
    }

    @Override
    public List<Stop> getStops() {
        logger.info("Get all stops");
        if (stops == null) { //to avoid NullPointerException
            return new ArrayList<>();
        }
        LiveEngine engine = ServiceRegistry.getService(LiveEngine.class);
        engine.enrichStops(stops.values());

        return new ArrayList<>(stops.values());
    }

    @Override
    public List<Tour> getTourSchedule(String line) {
        logger.info("Create tour scedule for line:" + line);
        TourStore store = ServiceRegistry.getService(TourStore.class);
        return store.getToursForLine(getLineForID(line));
    }

    @Override
    public List<Vehicle> getLineVehicles(String line) {
        logger.info("Fetch vehicles for line: " + line);
        FleetService fleet = ServiceRegistry.getService(FleetService.class);
        return fleet.getVehiclesForLine(getLineForID(line));
    }

    @Override
    public void addStopNotification(List<String> stopIDs, String lineID, String note) {
        //notification for at least two stops => we add it for the line
        Line line = getLine(lineID);
        if (line == null) {
            return;
        }
        if (allStopsOfTheLine(stopIDs, line)) {
            int uniqueNotificationID = determineUniqueNotificationID();
            Notification notification = new Notification(uniqueNotificationID, note, null, line);
            notePersistence.saveNotification(notification);
            this.notifications.add(notification);
        } else {
            for (String id : stopIDs) {
                Notification notification = new Notification(determineUniqueNotificationID(), note, stops.get(id), line);
                notePersistence.saveNotification(notification);
                this.notifications.add(notification);
            }
        }

    }

    private int determineUniqueNotificationID() {
        List<Notification> notifications = notePersistence.fetchNotifications();
        int highestNotificationID = 0;
        for (Notification oldNotification : notifications) {
            highestNotificationID = Math.max(highestNotificationID, oldNotification.getID());
        }
        return highestNotificationID + 1;
    }

    /**
     * @param stopIds which we have to check if they are all on the line
     * @param line    the line all stops should be on
     * @return whether all stops of the line are given and not more
     */
    private boolean allStopsOfTheLine(List<String> stopIds, Line line) {
        List<Stop> lineStops = line.getRoute().getStopSequence();
        // the exact same number of stops must be given...
        if (lineStops.size() != stopIds.size()) {
            return false;
        }
        //...and all stops must be on the line
        for (String stopId : stopIds) {
            boolean stopIsOnLine = false;
            for (Stop stop : lineStops) {
                if (stop.getId().equals(stopId)) {
                    stopIsOnLine = true;
                    break;
                }
            }
            if (!stopIsOnLine) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeNotification(List<String> stopIDs, String lineID, String deleteDescription) throws Exception {
        Line line = getLine(lineID);
        if (line == null) {
            return false;
        }
        // when this is true it means it is possible that there is only one notification for the whole line
        boolean singleLineDelete = allStopsOfTheLine(stopIDs, getLine(lineID));
        boolean multiDeleteWasSuccessful = false;
        List<Notification> notificationToDelete = new ArrayList<>();
        for (Notification note : notifications) {
            if (note.getTargetLine() != null && note.getTargetLine().equals(line) &&
                    note.getDescription().equals(deleteDescription)) {
                if (singleLineDelete && note.getTargetStop() == null) {
                    notificationToDelete.add(note);
                    break;
                }
                else if(note.getTargetStop() != null && stopIDs.contains(note.getTargetStopID())) {
                    notificationToDelete.add(note);
                }
            }
        }
        if (notificationToDelete.size() != 0) {
            for (Notification notification : notificationToDelete) {
                notifications.remove(notification);
                notePersistence.deleteNotification(notification);
                multiDeleteWasSuccessful = true;
            }
        }
        return multiDeleteWasSuccessful;
    }

    @Override
    public Line getLine(String targetLine) {
        return getLineForID(targetLine);
    }

    /**
     * This method return a line object for a given identifier
     *
     * @param id
     * @return
     */
    public Line getLineForID(String id) {
        for (Line line : lines) {
            if (line.getId().equals(id)) {
                return line;
            }
        }

        return null;
    }

    @Override
    public List<Stop> getStopsPerLine(String line) {
        return getLineForID(line).getRoute().getStopSequence();
    }

    @Override
    public List<Notification> getNotifications(String lineID) {
        List<Notification> lineNotes = new ArrayList<Notification>();
        Line actualLIne = getLineForID(lineID);

        for (Notification note : notifications) {
            if (note.getTargetLine() != null) {
                if (note.getTargetLine().getId().equals(lineID)) {
                    lineNotes.add(note);
                }
            } else {
                Stop actualStop = this.stops.get(note.getTargetStop().getId());
                if (actualStop != null && actualLIne.getRoute().getStopSequence().contains(actualStop)) {
                    lineNotes.add(note);
                }
            }

        }

        return lineNotes;
    }


    @Override
    public State getState() {
        List<State> problems = new ArrayList<State>();
        int errorStates = 0;
        int warningStates = 0;
        for (Line line : lines) {
            State lineState = line.getState();
            problems.add(lineState);
            errorStates = lineState.getType().equals(StateType.Red) ? errorStates + 1 : errorStates;
            warningStates = lineState.getType().equals(StateType.Yellow) ? warningStates + 1 : warningStates;

        }

        StateType state = warningStates > 0 ? StateType.Yellow : StateType.Green;

        state = errorStates >= 1 ? StateType.Red : state;

        return new State(state, null, problems);
    }

    public void setPersistence(LinePersistence line, StopPersistence stop, NotificationPersistence pers) {
        linePersistence = line;
        stopPersistence = stop;
        notePersistence = pers;

    }

    @Override
    public void updateStops(HashMap<String, List<TimedStopProblem>> problemsToAdd) {
        for (Stop stop : this.stops.values()) {
            if (problemsToAdd.containsKey(stop.getId())) {
                stop.addMultipleProblems(problemsToAdd.get(stop.getId()));
            }
        }
    }


    @Override
    public void updateLines(HashMap<String, List<TimedLineProblem>> problemsToAdd) {
        for (Line line : this.lines) {
            if (problemsToAdd.containsKey(line.getId())) {
                line.addMultipleProblems(problemsToAdd.get(line.getId()));
            }
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }


    @Override
    public void removeProblem(String stopOrLineId, Date time) throws Exception {
        if (getLineForID(stopOrLineId) != null) {
            List<TimedLineProblem> problems = getLineForID(stopOrLineId).getProblems();
            for (TimedLineProblem problem : problems) {
                if (problem.getDate().equals(time)) {
                    problems.remove(problem);
                    return;
                }
            }
        } else if (stops.get(stopOrLineId) != null) {
            List<TimedStopProblem> problems = stops.get(stopOrLineId).getProblems();
            for (TimedStopProblem problem : problems) {
                if (problem.getDate().equals(time)) {
                    problems.remove(problem);
                    return;
                }
            }
        }
        throw new Exception("The given id can't be found in system.");

    }

    @Override
    public Stop getStopPerID(String reference) {
        return stops.get(reference);
    }

    @Override
    public List<Map.Entry<Long, String>> getStopTimeTable(String stopId) throws Exception {
        List<Map.Entry<Long, String>> timeTableStop = new ArrayList<Map.Entry<Long, String>>();
        Stop stop = stops.get(stopId);
        if (stop == null) {
            throw new Exception("The given stop couldn't be found");
        }

        TourStore store = ServiceRegistry.getService(TourStore.class);
        for (Line line : lines) {
            if (line.getRoute().getStopSequence().contains(stop)) {
                List<Tour> tours = store.getToursForLine(line);
                for (Tour tour : tours) {
                    Date time = tour.getStopTime().get(stop);
                    timeTableStop.add(new AbstractMap.SimpleEntry<Long, String>(time.getTime(), line.getId()));
                }
            }
        }
        return timeTableStop;
    }

    public void setStopToClosedOrOpenedById(String stopId, boolean isClosed) {
        Stop stop = stops.get(stopId);
        if (stop == null) {
            logger.warn("This stop can not be set to closed or opened because it does not exist");
            return;
        }
        stop.setClosed(isClosed);
    }

    public boolean isStopClosedById(String stopId) throws Exception {
        Stop stop = stops.get(stopId);
        if (stop == null) {
            throw new Exception("This stop does not exist!");
        }
        return stop.isClosed();
    }

    public List<String> getAllClosedStopIds() {
        List<String> closedIds = new ArrayList<>();
        for (Stop stop : stops.values()) {
            if (stop.isClosed()) {
                closedIds.add(stop.getId());
            }
        }
        return closedIds;
    }

}
