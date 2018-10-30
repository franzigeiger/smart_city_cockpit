package de.se.model.mocks;

import de.se.DB.mocks.LineMock;
import de.se.DB.mocks.NotificationMock;
import de.se.DB.mocks.StopMock;
import de.se.data.*;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StateType;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleEnum;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;

import java.util.*;

public class NetworkMock implements Network{

    List<Line> lines;
    List<Stop> stops;
    List<Notification> notes;

    @Override
    public List<Line> getLines() {
        return lines;
    }

    @Override
    public List<Stop> getStops() {
        return stops;
    }

    @Override
    public List<Tour> getTourSchedule(String line) {
        return ServiceRegistry.getService(TourStore.class).getAllExistingTours();
    }

    @Override
    public List<Vehicle> getLineVehicles(String line) {
        return ServiceRegistry.getService(FleetService.class).getAllVehicles();
    }

    @Override
    public boolean removeNotification(List<String> stopIDs, String lineID, String deleteDescription) throws Exception {
        return true;
    }

    @Override
    public List<Stop> getStopsPerLine(String line) {
        return stops;
    }

    @Override
    public List<Notification> getNotifications(String lineID) {
        return notes;
    }

    @Override
    public void addStopNotification(List<String> stopIDs, String lineID, String note) {

    }


    @Override
    public Line getLine(String targetLine) {
        return lines.get(0);
    }

    @Override
    public void removeProblem(String stopOrLineId, Date time) throws Exception {

    }

    @Override
    public Stop getStopPerID(String reference) {
        return stops.get(0);
    }

    @Override
    public List<Map.Entry<Long, String>> getStopTimeTable(String stopId) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void initialize() {
        LineMock mock = new LineMock();
        lines=mock.fetchLines();
        StopMock mock1 = new StopMock();
        stops=mock1.fetchStops();
        NotificationMock mock2 = new NotificationMock();
        notes=mock2.fetchNotifications();

        add100LinesForLiveEngineTest();
        add100StopsForLiveEngineTest();

        for(Notification note : notes){
            note.setLine(lines.get(0));
        }
    }

    @Override
    public State getState() {
        State stopState = new State(StateType.Red, new ProblemElement("Stop", "one", Arrays.asList(new TimedStopProblem(StopProblem.Overflow))), null);
        List<State> newList = new ArrayList<State>();
        newList.add(stopState);
        State lineState= new State(StateType.Red,   new ProblemElement("Line","l1", Arrays.asList(new TimedLineProblem(LineProblem.Obstacle))), newList);
        List<State> lineList = new ArrayList<State>();
        lineList.add(lineState);
        return new State(StateType.Red , null , lineList);
    }

    @Override
    public void updateStops(HashMap<String, List<TimedStopProblem>> problemsToAdd) {

    }

    @Override
    public void updateLines(HashMap<String, List<TimedLineProblem>> problemsToAdd) {

    }

    public void add100StopsForLiveEngineTest() {
        for (int i = 0; i < 100; i++) {
            Stop stop = new Stop("ID " + i);
            List<TimedStopProblem> timedStopProblems = new ArrayList<>();
            timedStopProblems.add(new TimedStopProblem(StopProblem.Overflow)); //because it is deletable for the live engine
            stop.setProblems(timedStopProblems);
            this.stops.add(stop);
        }
    }

    public void add100LinesForLiveEngineTest() {
        for (int i = 0; i < 100; i++) {
            Line line = new Line("ID " + i, "Line " + i , VehicleEnum.Bus.toString(), new Route(new ArrayList<Stop>()), "red");
            List<TimedLineProblem> timedLineProblems = new ArrayList<>();
            timedLineProblems.add(new TimedLineProblem(LineProblem.General)); //because it is deletable for the live engine
            line.setProblems(timedLineProblems);
            this.lines.add(line);
        }
    }

    public void setStopToClosedOrOpenedById(String stopId, boolean isClosed) {

    }

    public boolean isStopClosedById(String stopId) throws Exception {
        return false;
    }

    public List<String> getAllClosedStopIds() {
        List<String> result = new ArrayList<>();
        result.add(stops.get(0).getId());
        return result;
    }
}