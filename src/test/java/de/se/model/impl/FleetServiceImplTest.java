package de.se.model.impl;

import de.se.DB.hibernate_models.Vehicledb;
import de.se.data.*;
import de.se.data.enums.StateType;
import de.se.data.enums.VehicleProblem;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.ServiceRegistry;
import de.se.model.mocks.VehiclePersistenceMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FleetServiceImplTest {
    static FleetServiceImpl fleet;
    static Vehicledb insertedVehicledb;

    @BeforeClass
    public static void setupClass() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        fleet = new FleetServiceImpl();
        fleet.setPersister(new VehiclePersistenceMock());
        fleet.initialize();
    }

    /**
     * cleans the database when there is still a dummy object from failed test
     */
    @Before
    public void cleanDBBefore() {
        fleet.persistence.deleteVehicle(new Vehicle(DummyTestObjectGenerator.getDummyVehicledb()));
    }

    /**
     * after each Test the tesVehicle will be deleted when it was not
     * since otherwise the next test could fail due to a existing DB object
     */
    @After
    public void destructClass() {
        if (insertedVehicledb != null)
            fleet.persistence.deleteVehicle(new Vehicle(insertedVehicledb));
    }

    @Test
    public void initialize() throws Exception {
        assertTrue(fleet.getAllVehicles().size() > 0);
    }

    @Test
    public void getVehiclesForLine() throws Exception {
        //TODO
    }

    @Test
    public void saveVehicle() throws Exception {
        int olfFleetSize = fleet.getAllVehicles().size();

        Vehicle vehicle = new Vehicle(DummyTestObjectGenerator.getDummyVehicledb());
        fleet.saveVehicle(vehicle);
        insertedVehicledb = vehicle.getVehicleDB();

        assertTrue(olfFleetSize == fleet.getAllVehicles().size() - 1);

        fleet.removeVehicle("testVehicle"); //remove from fleet
        fleet.persistence.deleteVehicle(new Vehicle(DummyTestObjectGenerator.getDummyVehicledb())); // delete DB object completely from DB
        insertedVehicledb = null;

        //check if it is really delete from the fleet
        assertTrue(olfFleetSize == fleet.getAllVehicles().size());
    }

    @Test
    public void getAllVehicles() throws Exception {
        //TODO
    }

    @Test
    public void getShiftPlan() throws Exception {
        //TODO
    }

    /**
     * This test might fail as well because of the savingMethod (check if saveVehicle() test passed)
     */
    @Test
    public void markVehicleAsDeleted() throws Exception {
        Vehicle vehicle = new Vehicle(DummyTestObjectGenerator.getDummyVehicledb());
        fleet.saveVehicle(vehicle);
        insertedVehicledb = vehicle.getVehicleDB();

        fleet.removeVehicle("testVehicle"); //remove from fleet
        assertTrue(vehicle.getVehicleDB().getDeleted());

        fleet.persistence.deleteVehicle(vehicle); // delete DB object completely from DB
        insertedVehicledb = null;
    }

    @Test
    public void getState() throws Exception {
        cleanProblems();

        State state = fleet.getState();
        int problems = 0;
        for (State vehicleState : state.getSubstates()) {
            problems += vehicleState.getProblem().getProblem().size();
        }

        assertEquals(state.getType(), StateType.Yellow);

        List<TimedVehicleProblem> vehicleProblems1 = new ArrayList<>();
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Accident));
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Defect_Engine));
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Defect_Door));

        HashMap<String, List<TimedVehicleProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("V1", vehicleProblems1);

        fleet.updateVehicles(problemsToAdd, new HashMap<String, Delay>());

        state = fleet.getState();
        assertEquals(state.getType(), StateType.Red);
        int allProblems = 0;
        for (State vehicleState : state.getSubstates()) {
            allProblems += vehicleState.getProblem().getProblem().size();
        }

        assertEquals(problems + 3, allProblems);
        //hardcore deleting of problems
        for (Vehicle vehicle : fleet.getNotLiveVehicles()) {
            fleet.getVehicleForId(vehicle.getId()).setProblems(new ArrayList<TimedVehicleProblem>());
        }

        state = fleet.getState();
        assertEquals(state.getType(), StateType.Green);

        allProblems = 0;
        for (State vehicleState : state.getSubstates()) {
            allProblems += vehicleState.getProblem().getProblem().size();
        }

        assertEquals(allProblems, 0);
    }

    @Test
    public void testUpdateVehicles() throws Exception {
        int numberOfVehicleProblemsBeforeUpdate = 0;

        // clean old problems
        cleanProblems();

        for (Vehicle vehicle : fleet.getAllVehicles()) {
            numberOfVehicleProblemsBeforeUpdate += vehicle.getProblems().size();
        }

        List<TimedVehicleProblem> vehicleProblems1 = new ArrayList<>();
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Accident));
        Thread.sleep(10);
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Defect_Engine));
        Thread.sleep(10);
        vehicleProblems1.add(new TimedVehicleProblem(VehicleProblem.Defect_Door));
        Thread.sleep(10);

        List<TimedVehicleProblem> vehicleProblems2 = new ArrayList<>();
        vehicleProblems2.add(new TimedVehicleProblem(VehicleProblem.Defect_Air_Conditioner));
        Thread.sleep(10);
        vehicleProblems2.add(new TimedVehicleProblem(VehicleProblem.Defect_Door));

        HashMap<String, List<TimedVehicleProblem>> problemsToAdd = new HashMap<>();
        problemsToAdd.put("V1", vehicleProblems1);
        problemsToAdd.put("V2", vehicleProblems2);

        HashMap<String, Delay> positionsWithDelays = new HashMap<>();
        positionsWithDelays.put("V3", new Delay(new Position("Stop 1", "Stop 2", true),
                new Position("Stop 1", "Stop 2", true), 60));
        positionsWithDelays.put("V4", new Delay(new Position("Stop 2", "Stop 3", false),
                new Position("Stop 2", "Stop 3", false), 60));
        fleet.updateVehicles(problemsToAdd, positionsWithDelays);

        int numberOfVehicleProblemsAfterUpdate = 0;
        for (Vehicle vehicle : fleet.getAllVehicles()) {
            numberOfVehicleProblemsAfterUpdate += vehicle.getProblems().size();
        }

        assertEquals(numberOfVehicleProblemsBeforeUpdate + 5, numberOfVehicleProblemsAfterUpdate);

        for (Vehicle vehicle : fleet.getAllVehicles()) {
            if (vehicle.getId().equals("V3")) {
                assertEquals("Stop 1", vehicle.getActualPosition().getPrevStop());
                assertEquals("Stop 2", vehicle.getActualPosition().getNextStop());
            }

            if (vehicle.getId().equals("V4")) {
                assertEquals("Stop 2", vehicle.getActualPosition().getPrevStop());
                assertEquals("Stop 3", vehicle.getActualPosition().getNextStop());
            }
        }

        for (TimedVehicleProblem problem : vehicleProblems1) {
            fleet.removeVehicleProblem("V1", problem.getDate());
        }

        for (TimedVehicleProblem problem : vehicleProblems2) {
            fleet.removeVehicleProblem("V2", problem.getDate());
        }

        numberOfVehicleProblemsAfterUpdate = 0;
        for (Vehicle vehicle : fleet.getAllVehicles()) {
            numberOfVehicleProblemsAfterUpdate += vehicle.getProblems().size();
        }

        assertEquals(numberOfVehicleProblemsBeforeUpdate, numberOfVehicleProblemsAfterUpdate);
    }

    @Test
    public void testGetVehicleForId() {
        assertNotNull(fleet.getVehicleForId("V1"));
        assertNull(fleet.getVehicleForId("NotExistingId"));
    }

    @Test
    public void testGetLinesForVehicle() {
        assertTrue(fleet.getLinesForVehicle(fleet.getVehicleForId("V1")).size() > 0);
    }


    @Test
    public void testRemoveProblem() {
        TimedVehicleProblem problem = new TimedVehicleProblem(VehicleProblem.Defect_Door);
        fleet.getVehicleForId("V1").addProblem(problem);

        int sizeBeforeDeletion = fleet.getVehicleForId("V1").getProblems().size();

        fleet.removeVehicleProblem("V1", problem.getDate());

        int sizeAfterDeletion = fleet.getVehicleForId("V1").getProblems().size();

        assertEquals(sizeBeforeDeletion - 1, sizeAfterDeletion);

    }

    private void cleanProblems() {
        Vehicle v1 = fleet.getVehicleForId("V1");
        v1.setProblems(null);
        Vehicle v2 = fleet.getVehicleForId("V1");
        v1.setProblems(null);
    }

}