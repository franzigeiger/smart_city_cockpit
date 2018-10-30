package de.se.model.impl;

import de.se.data.*;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleProblem;
import de.se.model.LiveEngineCreativity;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import de.se.model.mocks.NetworkMock;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LiveEngineImplTest {

    private Logger logger = Logger.getLogger(LiveEngineImplTest.class);
    private static LiveEngineImpl liveEngine = new LiveEngineImpl();

    @BeforeClass
    public static void startUp() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        liveEngine.setLIVE_ENGINE_RUN_LIVE(false); //we want to prevent the live engine from running automatically every x seconds
        liveEngine.initialize();
    }

    @Test
    public void testStartGenerateAndDeleteProblems() throws Exception { //this tests whether the configuration variables LIVE_ENGINE... work
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        // start a new LiveEngine to simulate a new startUp (otherwise there can be already many problems created by the other test
        this.liveEngine.setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(0);
        liveEngine = new LiveEngineImpl();
        liveEngine.setLIVE_ENGINE_RUN_LIVE(false); //we want to prevent the live engine from running automatically every x seconds
        liveEngine.initialize();
        liveEngine.start();

        int amountOfVehicleProblems = this.liveEngine.computeNumberOfVehicleProblems();
        int amountOfStopProblems = this.liveEngine.computeNumberOfStopProblems();
        int amountOfLineProblems = this.liveEngine.computeNumberOfLineProblems();

        logger.info("Vehicle: " + amountOfVehicleProblems + ", stop: " + amountOfStopProblems +
                ", line: " + amountOfLineProblems);

        Assert.assertEquals((int) Math.round(fleetService.getAllVehicles().size() *
                        ((double) liveEngine.getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE()) / 100),
                amountOfVehicleProblems);
        Assert.assertEquals((int) Math.round(network.getStops().size() *
                        ((double) liveEngine.getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE()) / 100),
                amountOfStopProblems);
        Assert.assertEquals((int) Math.round(network.getLines().size() *
                        ((double) liveEngine.getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE()) / 100),
                amountOfLineProblems);

        liveEngine.flushDataToServices();


        /*Now we generate problems according to the LIVE_ENGINE_NUMBER_OF... settings and check if the fleet
          and the network got all the information.
          This method is usually called repeatedly and regularly in the run() method of the live engine thread*/
        liveEngine.generateProblems();

        int amountOfVehicleProblemsAfterGenerating = this.liveEngine.computeNumberOfVehicleProblems();
        int amountOfStopProblemsAfterGenerating = this.liveEngine.computeNumberOfStopProblems();
        int amountOfLineProblemsAfterGenerating = this.liveEngine.computeNumberOfLineProblems();

        Assert.assertEquals(liveEngine.getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(),
                amountOfVehicleProblemsAfterGenerating);

        Assert.assertEquals(liveEngine.getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(),
                amountOfStopProblemsAfterGenerating);

        Assert.assertEquals(liveEngine.getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(),
                amountOfLineProblemsAfterGenerating);

        logger.info("Vehicle: " + amountOfVehicleProblemsAfterGenerating + ", stop: " +
                amountOfStopProblemsAfterGenerating + ", line: " + amountOfLineProblemsAfterGenerating);

        // unfortunately does not work really since the ServiceRegistry is in "test"-mode
        // but as long as it does not crashes everything is fine
        liveEngine.restart();
    }

    @Test
    public void testGenerateAndDeleteVehicleProblems() throws Exception {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);

        int numberOfProblemsBeforeGenerating = 0;
        HashMap<String, List<TimedVehicleProblem>> vehicleProblems = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            List<TimedVehicleProblem> vehicleProblemsList = new ArrayList<>();
            vehicleProblemsList.add(new TimedVehicleProblem(VehicleProblem.getRandomVehicleProblem()));
            vehicleProblemsList.add(new TimedVehicleProblem(VehicleProblem.getRandomVehicleProblem()));
            vehicleProblemsList.add(new TimedVehicleProblem(VehicleProblem.Accident)); //because they are deletable for later on
            numberOfProblemsBeforeGenerating += vehicleProblemsList.size();
            vehicleProblems.put("ID " + i, vehicleProblemsList);
        }

        this.liveEngine.setNewVehicleProblems(vehicleProblems);

        for (int i = 0; i < 50; i++) { //generate 50 stop problems
            this.liveEngine.generateVehicleProblem();
        }

        int numberOfProblemsAfterGenerating = 0;
        for (List<TimedVehicleProblem> finalVehicleProblems : vehicleProblems.values()) {
            numberOfProblemsAfterGenerating += finalVehicleProblems.size();
            for (TimedVehicleProblem finalVehicleProblem : finalVehicleProblems) {
                Date dateOfVehicleProblem = finalVehicleProblem.getDate();
                Assert.assertNotNull(finalVehicleProblem.getVehicleProblem());
                Assert.assertNotNull(dateOfVehicleProblem);
                Assert.assertTrue(dateOfVehicleProblem.getTime() == new Date().getTime() || dateOfVehicleProblem.before(new Date()));
            }
        }

        Assert.assertEquals(numberOfProblemsBeforeGenerating + 50, numberOfProblemsAfterGenerating);

        //now we add the problems to the live engine and delete them

        this.liveEngine.setNewVehicleProblems(vehicleProblems);


        //fill vehicles in fleet with problems (in a non-mock environment this is already given)
        for (String key : this.liveEngine.getNewVehicleProblems().keySet()) {
            List<TimedVehicleProblem> timedVehicleProblems = this.liveEngine.getNewVehicleProblems().get(key);
            for (Vehicle vehicle : fleetService.getAllVehicles()) {
                if (vehicle.getId().equals(key)) {
                    vehicle.setProblems(timedVehicleProblems);
                    break;
                }
            }
        }

        logger.info("Problems before deleting: " + numberOfProblemsAfterGenerating);

        int actuallyDeletedProblems = this.liveEngine.deleteVehicleProblems(100);

        Assert.assertEquals(100, actuallyDeletedProblems); //because we added 100 VehicleProblem.Accident problems before

        int numberOfProblemsAfterDeleting = 0;
        HashMap<String, List<TimedVehicleProblem>> newVehicleProblemsInLiveEngine = this.liveEngine.getNewVehicleProblems();
        for (String vehicle : newVehicleProblemsInLiveEngine.keySet()) {
            numberOfProblemsAfterDeleting += newVehicleProblemsInLiveEngine.get(vehicle).size();
        }
        logger.info("Problems after deleting: " + numberOfProblemsAfterDeleting);
    }

    @Test
    public void testGenerateAndDeleteStopProblems() throws Exception {
        int numberOfProblemsBeforeGenerating = 0;
        HashMap<String, List<TimedStopProblem>> stopProblems = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            List<TimedStopProblem> stopProblemsList = new ArrayList<>();
            stopProblemsList.add(new TimedStopProblem(StopProblem.getRandomStopProblem()));
            stopProblemsList.add(new TimedStopProblem(StopProblem.getRandomStopProblem()));
            numberOfProblemsBeforeGenerating += stopProblemsList.size();
            stopProblems.put("ID " + i, stopProblemsList);
        }

        this.liveEngine.setNewStopProblems(stopProblems);

        for (int i = 0; i < 50; i++) { //generate 50 stop problems
            this.liveEngine.generateStopProblem();
        }

        int numberOfProblemsAfterGenerating = 0;
        for (List<TimedStopProblem> finalStopProblems : stopProblems.values()) {
            numberOfProblemsAfterGenerating += finalStopProblems.size();
            for (TimedStopProblem finalStopProblem : finalStopProblems) {
                Date dateOfStopProblem = finalStopProblem.getDate();
                Assert.assertNotNull(finalStopProblem.getStopProblem());
                Assert.assertNotNull(dateOfStopProblem);
                Assert.assertTrue(dateOfStopProblem.getTime() == new Date().getTime() || dateOfStopProblem.before(new Date()));
            }
        }

        Assert.assertEquals(numberOfProblemsBeforeGenerating + 50, numberOfProblemsAfterGenerating);

        //now we add the problems to the live engine and delete them

        this.liveEngine.setNewStopProblems(stopProblems);

        NetworkMock networkMock = new NetworkMock();
        networkMock.initialize();
        networkMock.add100StopsForLiveEngineTest();

        //fill stops in network with problems (in a non-mock environment this is already given)
        for (String key : this.liveEngine.getNewStopProblems().keySet()) {
            List<TimedStopProblem> timedStopProblems = this.liveEngine.getNewStopProblems().get(key);
            for (Stop stop : networkMock.getStops()) {
                if (stop.getId().equals(key)) {
                    stop.setProblems(timedStopProblems);
                    break;
                }
            }
        }

        logger.info("Problems before deleting: " + numberOfProblemsAfterGenerating);

        int actuallyDeletedProblems = this.liveEngine.deleteStopProblems(100);
        Assert.assertEquals(100, actuallyDeletedProblems);
    }

    @Test
    public void testGenerateAndDeleteLineProblems() throws Exception {
        Network network = ServiceRegistry.getService(Network.class);

        int numberOfProblemsBeforeGenerating = 0;
        HashMap<String, List<TimedLineProblem>> lineProblems = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            List<TimedLineProblem> lineProblemsList = new ArrayList<>();
            lineProblemsList.add(new TimedLineProblem(LineProblem.getRandomLineProblem()));
            lineProblemsList.add(new TimedLineProblem(LineProblem.getRandomLineProblem()));
            lineProblemsList.add(new TimedLineProblem(LineProblem.Obstacle));
            numberOfProblemsBeforeGenerating += lineProblemsList.size();
            lineProblems.put("ID " + i, lineProblemsList);
        }

        this.liveEngine.setNewLineProblems(lineProblems);

        for (int i = 0; i < 50; i++) { //generate 50 stop problems
            this.liveEngine.generateLineProblem();
        }

        int numberOfProblemsAfterGenerating = 0;
        for (List<TimedLineProblem> finalLineProblems : lineProblems.values()) {
            numberOfProblemsAfterGenerating += finalLineProblems.size();
            for (TimedLineProblem finalLineProblem : finalLineProblems) {
                Date dateOfLineProblem = finalLineProblem.getDate();
                Assert.assertNotNull(finalLineProblem.getLineProblem());
                Assert.assertNotNull(dateOfLineProblem);
                Assert.assertTrue(dateOfLineProblem.getTime() == new Date().getTime() || dateOfLineProblem.before(new Date()));
            }
        }

        Assert.assertEquals(numberOfProblemsBeforeGenerating + 50, numberOfProblemsAfterGenerating);

        //now we add the problems to the live engine and delete them

        this.liveEngine.setNewLineProblems(lineProblems);

        NetworkMock networkMock = (NetworkMock) network;
        networkMock.initialize();
        networkMock.add100LinesForLiveEngineTest();

        //fill lines in network with problems (in a non-mock environment this is already given)
        for (String key : this.liveEngine.getNewLineProblems().keySet()) {
            List<TimedLineProblem> timedLineProblems = this.liveEngine.getNewLineProblems().get(key);
            for (Line line : networkMock.getLines()) {
                if (line.getId().equals(key)) {
                    line.setProblems(timedLineProblems);
                    break;
                }
            }
        }

        logger.info("Problems before deleting: " + numberOfProblemsAfterGenerating);

        int actuallyDeletedProblems = this.liveEngine.deleteLineProblems(100);
        Assert.assertEquals(100, actuallyDeletedProblems); //because we manually added LineProblem.Obstacle (which is deletable) 100 times
    }


    @Test
    public void testGeneratePositionsAndDelays() throws Exception {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        TourStore tourStore = ServiceRegistry.getService(TourStore.class);

        List<Vehicle> vehicles = fleetService.getAllVehicles();
        // change the vehicle to only 2 since the TourStore Mock only returns tours for 2 vehicles
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId().equals("V1") || vehicle.getId().equals("V2")) {
                newVehicles.add(vehicle);
            }
        }
        vehicles = newVehicles;

        HashMap<String, Position> positions = this.liveEngine.generatePositions(vehicles);
        // is not correct any more since we only look on 2 vehicles
        //Assert.assertEquals(fleetService.getAllVehicles().size(), positions.size());

        for (Vehicle vehicle : vehicles) { //check if we have positions for all vehicles
            Assert.assertTrue(positions.containsKey(vehicle.getId()));
            List<Tour> tours = tourStore.getAllExistingTours();
            Assert.assertTrue(positions.get(vehicle.getId()).isOnPrev());

            if (vehicle.getId().equals("V1")) { //all tours are for vehicle with ID "V1"
                Assert.assertTrue(positions.get(vehicle.getId()).getPrevStop().equals(tours.get(0).getStartStop()));
                Assert.assertTrue(positions.get(vehicle.getId()).getNextStop().equals(tours.get(0).getEndStop()));
            }
        }

        //Now we will generate delays

        this.liveEngine.setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(100);
        this.liveEngine.setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(0);
        if (! liveEngine.isLiveEngineStarted()) {
            liveEngine.start();
        }
        else {
            liveEngine.restart();
        }
        HashMap<String, Delay> delays = this.liveEngine.generateDelays(vehicles);
        for (Vehicle vehicle : vehicles) {
            // debug logger for travis
            //logger.warn("Travis debugging -> keys: " + delays.keySet().toString() + ",values:" + delays.values().toString());
            Assert.assertTrue(delays.containsKey(vehicle.getId()));
            Delay delay = delays.get(vehicle.getId());

            //expected position should be the original one
            Assert.assertEquals(positions.get(vehicle.getId()).getPrevStop(), delay.getExpectedPosition().getPrevStop());
            Assert.assertEquals(positions.get(vehicle.getId()).getNextStop(), delay.getExpectedPosition().getNextStop());

            Assert.assertEquals("Prev stop for delay testing", delay.getActualPosition().getPrevStop());
            Assert.assertEquals("Next stop for delay testing", delay.getActualPosition().getNextStop());
            Assert.assertEquals(false, delay.getActualPosition().isOnPrev());

            Assert.assertTrue(delay.getDelay() >= 60); //minimum delay: one minute
        }
    }

    @Test
    public void testGetNewFeedbacksAndSaveFeedback() throws InterruptedException {
        int createFeedback = 10;
        this.liveEngine.setLiveEngineCreativity(new LiveEngineCreativity());
        this.liveEngine.setLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(createFeedback);

        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);
        int numberOfFeedbackBefore = feedbackService.getAllFeedbacks().size();
        //this.liveEngine.getFeedbackService().initialize();
        //this.liveEngine.getFeedbackService().removeAllFeedbacksLocally();

        int sizeBeforeSaving = this.liveEngine.getNewFeedbacks().size(); //call the method to update "this.liveEngine.lastCallOfNewFeedbacksMethod"
        Thread.sleep(10);
        //now we generate and save feedbacks
        this.liveEngine.generateFeedbacks();
        Thread.sleep(10);
        /*logger.info(("Last call of feedbacks method: " + new Timestamp(this.liveEngine.getLastCallOfNewFeedbacksMethod()));
        logger.info(("Inserted feedback timestamp: " + feedback.getTimeStamp());
        logger.info(("Now: " + new Timestamp(new Date().getTime()));*/

        int sizeAfterSaving = this.liveEngine.getNewFeedbacks().size();

        Thread.sleep(10);
        Assert.assertEquals( this.liveEngine.getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(), sizeAfterSaving);

        int sizeAfterCallingAgain = this.liveEngine.getNewFeedbacks().size();
        Assert.assertEquals(0, sizeAfterCallingAgain);

        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();
        //delete all created feedback
        for (int i = 0; i < createFeedback; i++) {
            feedbackService.removeFeedback(allFeedbacks.get(allFeedbacks.size() - 1)); //remove the newly added feedback again
        }
        Assert.assertEquals(numberOfFeedbackBefore, feedbackService.getAllFeedbacks().size());
    }

    @Test
    public void testDeleteVehicleProblems() {

    }
}