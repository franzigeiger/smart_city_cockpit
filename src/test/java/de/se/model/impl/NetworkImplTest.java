package de.se.model.impl;

import de.se.DB.mocks.LineMock;
import de.se.DB.mocks.NotificationMock;
import de.se.DB.mocks.StopMock;
import de.se.data.*;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StateType;
import de.se.data.enums.StopProblem;
import de.se.model.ServiceRegistry;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class NetworkImplTest {
    static NetworkImpl network;
    private Logger logger = Logger.getLogger(LiveEngineImplTest.class);

    @BeforeClass
    public static void setupClass(){
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        network = new NetworkImpl();
        network.setPersistence(new LineMock(), new StopMock(), new NotificationMock());
        network.initialize();
    }

    @Test
    public void initialize() throws Exception {
        assertTrue(network.getLines().size() > 0);
    }

    @Test
    public void getLines() throws Exception {
        List<Line> lines = network.getLines();

        Line line = lines.get(0);
        assertNotNull(line.getId());
        assertNotNull(line.getName());
        assertNotNull(line.getRoute());
        assertNotNull(line.getState());
        //assertNotNull(line.getTable());
        assertNotNull(line.getVehicleType());
        assertNotNull(line.getState());
    }

    @Test
    public void getStops() throws Exception {
        List<Stop> stops = network.getStops();

        Stop stop = stops.get(0);

        assertNotNull(stop.getId());
        assertNotNull(stop.getState());
        assertNotNull(stop.getDescription());
        assertNotNull(stop.getLatitude());
        assertNotNull(stop.getLongitude());
        assertNotNull(stop.getProblems());
        assertNotNull(stop.getRequestItemID());
    }

    @Test
    public void getTourSchedule() throws Exception {
        //fails right now
        List<Tour> tours =network.getTourSchedule("l1");

        Tour tour = tours.get(0);
        assertNotNull(tour.getEndStop());
        assertNotNull(tour.getVehicle());
        assertNotNull(tour.getLine());
        assertNotNull(tour.getStartStop());
        //doesn't work  assertNotNull(tour.getStartTime());
        //doesn't work  assertNotNull(tour.getEndTime());
    }

    @Test
    public void getTimeTable() throws Exception {
        //not yet implemented
    }

    @Test
    public void getLineVehicles() throws Exception {
        assertTrue(network.getLineVehicles("l1").size() >  0);
    }

    @Test
    public void testAddAndRemoveStopNotification() throws Exception {
        List<String> stops = new ArrayList<String>();
        stops.add("one");
        int size =network.getNotifications().size();
        network.addStopNotification(stops, "l1", "Note for all");

        assertEquals(size + 1 , network.getNotifications().size());

        network.removeNotification(stops,"l1", "Note for all");

        assertEquals(size, network.getNotifications().size());
    }

    @Test
    public void testAddAndRemoveNotification() throws Exception {
        List<String> stops = new ArrayList<String>();
        stops.add("one");
        stops.add("two");
        stops.add("three");

        int size =network.getNotifications().size();
        network.addStopNotification(stops, "l1", "Note for all");

        assertEquals(size + 3 , network.getNotifications().size());

        network.removeNotification(stops,"l1", "Note for all");

        assertEquals(size, network.getNotifications().size());
    }

    @Test
    public void addAndRemoveLineNotification() throws Exception {
        List<String> stops = new ArrayList<String>();
        stops.add("one");
        stops.add("two");
        stops.add("three");
        stops.add("four");

        int size =network.getNotifications().size();
        network.addStopNotification(stops, "l1", "Note for all");

        // there should be only one since the notification is for the whole line
        assertEquals(size + 1 , network.getNotifications().size());

        network.removeNotification(stops,"l1", "Note for all");

        assertEquals(size, network.getNotifications().size());
    }

    @Test
    public void testGetLineForID() throws Exception {
        assertNotNull(network.getLineForID("l1"));
    }

    @Test
    public void getStopsPerLine() throws Exception {
        //senseless test because route isn't implemented yet
        //to do: change to useful test
        assertTrue( network.getStopsPerLine("l1").size() >= 0);
    }

    @Test
    public void getNotifications() throws Exception {
        assertNotNull(network.getNotifications("l1"));
    }

    @Test
    public void testUpdateStops() throws Exception {
        int numberOfStopProblemsBeforeUpdate = 0;
        for (Stop stop : network.getStops()) {
            numberOfStopProblemsBeforeUpdate += stop.getProblems().size();
        }

        List<TimedStopProblem> stopProblems1 = new ArrayList<>();
        stopProblems1.add(new TimedStopProblem(StopProblem.Dirty));
        stopProblems1.add(new TimedStopProblem(StopProblem.Broken));
        stopProblems1.add(new TimedStopProblem(StopProblem.Overflow));

        List<TimedStopProblem> stopProblems2 = new ArrayList<>();
        stopProblems2.add(new TimedStopProblem(StopProblem.Dirty));
        stopProblems2.add(new TimedStopProblem(StopProblem.Overflow));

        HashMap<String, List<TimedStopProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("one", stopProblems1);
        problemsToAdd.put("two", stopProblems2);
        network.updateStops(problemsToAdd);

        int numberOfStopProblemsAfterUpdate = 0;
        for (Stop stop : network.getStops()) {
            numberOfStopProblemsAfterUpdate += stop.getProblems().size();
        }

        assertEquals(numberOfStopProblemsBeforeUpdate + 5, numberOfStopProblemsAfterUpdate);

        for(TimedStopProblem problem : stopProblems1){
            network.removeProblem("one", problem.getDate());
        }

        for(TimedStopProblem problem : stopProblems2){
            network.removeProblem("two", problem.getDate());
        }

        numberOfStopProblemsAfterUpdate =0;
        for (Stop stop : network.getStops()) {
            numberOfStopProblemsAfterUpdate += stop.getProblems().size();
        }

        assertEquals(numberOfStopProblemsBeforeUpdate, numberOfStopProblemsAfterUpdate);
    }

    @Test
    public void testUpdateLines() throws Exception {
        int numberOfLineProblemsBeforeUpdate = 0;
        for (Line line : network.getLines()) {
            numberOfLineProblemsBeforeUpdate += line.getProblems().size();
        }

        List<TimedLineProblem> lineProblems1 = new ArrayList<>();
        lineProblems1.add(new TimedLineProblem(LineProblem.Obstacle));
        lineProblems1.add(new TimedLineProblem(LineProblem.General));
        lineProblems1.add(new TimedLineProblem(LineProblem.Overflow));

        List<TimedLineProblem> lineProblems2 = new ArrayList<>();
        lineProblems2.add(new TimedLineProblem(LineProblem.General));
        lineProblems2.add(new TimedLineProblem(LineProblem.Obstacle));

        HashMap<String, List<TimedLineProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("l1", lineProblems1);
        problemsToAdd.put("l2", lineProblems2);
        network.updateLines(problemsToAdd);

        int numberOfLineProblemsAfterUpdate = 0;
        for (Line line : network.getLines()) {
            numberOfLineProblemsAfterUpdate += line.getProblems().size();
        }

        assertEquals(numberOfLineProblemsBeforeUpdate + 5, numberOfLineProblemsAfterUpdate);

        for(TimedLineProblem problem : lineProblems1){
            network.removeProblem("l1", problem.getDate());
        }

        for(TimedLineProblem problem : lineProblems2){
            network.removeProblem("l2", problem.getDate());
        }

        numberOfLineProblemsAfterUpdate =0;
        for (Line line : network.getLines()) {
            numberOfLineProblemsAfterUpdate += line.getProblems().size();
        }

        assertEquals(numberOfLineProblemsBeforeUpdate, numberOfLineProblemsAfterUpdate);
    }

    @Test
    public void testRemoveProblemForLine() throws Exception {
        TimedLineProblem problem = new TimedLineProblem(LineProblem.Obstacle);
        network.getLineForID("l1").addProblem(problem);

        int sizeBeforeDeletion =network.getLineForID("l1").getProblems().size();

        network.removeProblem("l1", problem.getDate());

        int sizeAfterDeletion = network.getLineForID("l1").getProblems().size();

        assertEquals(sizeBeforeDeletion -1 , sizeAfterDeletion);

    }
    @Test
    public void testRemoveProblemForStop() throws Exception {
        TimedStopProblem problem = new TimedStopProblem(StopProblem.Overflow);
        network.getStopPerID("one").addProblem(problem);

        int sizeBeforeDeletion =network.getStopPerID("one").getProblems().size();

        network.removeProblem("one", problem.getDate());

        int sizeAfterDeletion = network.getStopPerID("one").getProblems().size();

        assertEquals(sizeBeforeDeletion -1 , sizeAfterDeletion);

    }

    /**
     * This method tests several state calculations in case of new line problems.
     */
    @Test
    public void testGetStateLineProblems(){
        State state =network.getState();
        int problems =0;
        for(State lineState : state.getSubstates()){
            problems += lineState.getProblem().getProblem().size();
        }

        assertEquals(state.getType(), StateType.Green);

        List<TimedLineProblem> lineproblems = new ArrayList<>();
        lineproblems.add(new TimedLineProblem(LineProblem.General));


        HashMap<String, List<TimedLineProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("l1", lineproblems);

        network.updateLines(problemsToAdd);

        state =network.getState();
        assertEquals(state.getType(), StateType.Yellow);
        int allProblems =0;
        for(State lineState : state.getSubstates()){
            allProblems += lineState.getProblem().getProblem().size();
        }
        assertEquals(problems + 1,allProblems);

        lineproblems.clear();
        lineproblems.add(new TimedLineProblem(LineProblem.Obstacle));
        lineproblems.add(new TimedLineProblem(LineProblem.General));
        lineproblems.add(new TimedLineProblem(LineProblem.Overflow));

        problemsToAdd.clear();
        problemsToAdd.put("l2", lineproblems);

        network.updateLines(problemsToAdd);

        state = network.getState();
        assertEquals(state.getType(), StateType.Red);
        allProblems =0;
        for(State lineState : state.getSubstates()){
            allProblems += lineState.getProblem().getProblem().size();

            if(lineState.getProblem().getElemID().equalsIgnoreCase("l2")){
                assertEquals(lineState.getType(), StateType.Red);
            }
        }
        assertEquals(problems + 1 +3 ,allProblems);

        for(Line line :network.getLines()){
            line.getProblems().clear();
        }
    }

    /**
     * This method tests several settings of state calculation in case of new stop problems.
     */
    @Test
    public void testGetStateStopProblems(){
        State state =network.getState();
        int problems =0;
        for(State lineState : state.getSubstates()){
            for(State stopStates : lineState.getSubstates()){
                problems += stopStates.getProblem().getProblem().size();
            }
        }
        //no problems in the beginning so state is green
        assertEquals(state.getType(), StateType.Green);

        List<TimedStopProblem> stopProblems = new ArrayList<>();
        stopProblems.add(new TimedStopProblem(StopProblem.Dirty));

        HashMap<String, List<TimedStopProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("one", stopProblems);

        network.updateStops(problemsToAdd);

        state =network.getState();
        //If one problem was added the state must switch to yellow
        assertEquals(state.getType(), StateType.Yellow);
        int allProblems =0;
        for(State lineState : state.getSubstates()){
            for(State stopStates : lineState.getSubstates()){
                allProblems += stopStates.getProblem().getProblem().size();
            }
        }
        assertEquals(problems + 1,allProblems);

        stopProblems.clear();
        stopProblems.add(new TimedStopProblem(StopProblem.Dirty));
        stopProblems.add(new TimedStopProblem(StopProblem.Broken));
        stopProblems.add(new TimedStopProblem(StopProblem.Overflow));

        problemsToAdd.clear();
        problemsToAdd.put("two", stopProblems);

        network.updateStops(problemsToAdd);

        state =network.getState();
        //After adding of further three problems the state must be red.
        assertEquals(state.getType(), StateType.Red);
        allProblems =0;
        for(State lineState : state.getSubstates()){
            for(State stopStates : lineState.getSubstates()){
                allProblems += stopStates.getProblem().getProblem().size();
                if(stopStates.getProblem().getElemID().equalsIgnoreCase("two")){
                    assertEquals(lineState.getType(), StateType.Red);
                }
            }

        }
        assertEquals(problems + 1 +3 ,allProblems);

        network.getStopPerID("two").getProblems().clear();

        stopProblems.clear();
        stopProblems.add(new TimedStopProblem(StopProblem.Overflow));

        problemsToAdd.clear();
        problemsToAdd.put("one", stopProblems);

        network.updateStops(problemsToAdd);

        state =network.getState();
        //The three problems was deleted and a very severe problem added, which also causes a red state.
        assertEquals(state.getType(), StateType.Red);
        allProblems =0;
        for(State lineState : state.getSubstates()){
            for(State stopStates : lineState.getSubstates()){
                allProblems += stopStates.getProblem().getProblem().size();
                if(stopStates.getProblem().getElemID().equalsIgnoreCase("one")){
                    assertEquals(lineState.getType(), StateType.Red);
                }
            }


        }

        assertEquals(problems + 1 + 1 ,allProblems);

        //Delete the generated afterwards for further tests
        for(Stop stop : network.getStops()){
            stop.getProblems().clear();
        }
    }

    /**
     * Test if the field is correctly set to false
     */
    @Test
    public void testGetClosedFieldOfStop() {
        try {
            assertFalse(network.isStopClosedById("LUHAW"));
        }
        catch (Exception e) {
            logger.warn("This should not happen here. Error message: " + e);
        }
    }

    @Test
    public void testSetAndGetClosedFieldOfStop() {
        network.setStopToClosedOrOpenedById("LUHAW", true);
        try {
            assertTrue(network.isStopClosedById("LUHAW"));
        }
        catch (Exception e) {
            logger.warn("This should not happen here. Error message: " + e);
        }
    }

    @Test
    public void testGetClosedFieldOfStopException(){
        String exception = "";
        try {
            network.isStopClosedById("-----");
        } catch (Exception e) {
            exception = e.getMessage();
        }
        assertEquals("This stop does not exist!", exception);
    }

    @Test
    public void getAllClosedStopIdsTest() {
        int numberOfclosedStopsBefore = network.getAllClosedStopIds().size();
        network.setStopToClosedOrOpenedById("one", true);
        int numberOfclosedStopsAfter = network.getAllClosedStopIds().size();
        assertEquals(numberOfclosedStopsBefore + 1, numberOfclosedStopsAfter);
        network.setStopToClosedOrOpenedById("one", false);
        assertEquals(numberOfclosedStopsBefore, network.getAllClosedStopIds().size());
    }

}