package de.se.model.impl;

import de.se.data.*;
import de.se.data.enums.FeedbackEnum;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleProblem;
import de.se.model.LiveEngineCreativity;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.*;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class LiveEngineImpl extends TimerTask implements LiveEngine {

    /*
     * Configuration variables for Live Engine
     */

    /**
     * The interval (in seconds) in which the live engine should generate problems and positions
     */
    private int LIVE_ENGINE_FREQUENCY_SECONDS = 3 * 60;

    /**
     * The number of vehicle problems the live engine should generate in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN = 5;

    /**
     * The number of stop problems the live engine should generate in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN = 3;

    /**
     * The number of line problems the live engine should generate in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN = 1;

    /**
     * The number of feedbacks the live engine should generate in each run
     */

    private int LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN = 1;

    /**
     * The number of vehicle problems the live engine should delete in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN = 2;

    /**
     * The number of stop problems the live engine should delete in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN = 1;

    /**
     * The number of line problems the live engine should delete in each run
     */
    private int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN = 1;

    /**
     * The percentage of vehicles that should have a problem at startup of the live engine.
     * Setting this to x does not mean that x% of vehicles will have one problem each, but rather that
     * there will be a total of x% * (number of vehicles) problems (rounded to an integer) in the fleet.
     * Some vehicles might have more than one problem.
     */
    private int LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE = 20;

    /**
     * The percentage of stops that should have a problem at startup of the live engine.
     * Setting this to x does not mean that x% of stops will have one problem each, but rather that
     * there will be a total of x% * (number of stops) problems (rounded to an integer) in the fleet.
     * Some stops might have more than one problem.
     */
    private int LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE = 10;

    /**
     * The percentage of lines that should have a problem at startup of the live engine.
     * Setting this to x does not mean that x% of lines will have one problem each, but rather that
     * there will be a total of x% * (number of lines) problems (rounded to an integer) in the fleet.
     * Some lines might have more than one problem.
     */
    private int LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE = 40;

    /**
     * The percentage of current running tours that should have a delay at startup of the live engine.
     * Setting this to x does not mean that x% of lines will have one problem each, but rather that
     * there will be a total of x% * (number of lines) problems (rounded to an integer) in the fleet.
     * Some lines might have more than one problem.
     */
    private int LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE = 20;

    /**
     * The probability of a current running tour to get a delay in each run
     */
    private int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN = 25;

    /**
     * The probability of a current running tour with delay to decrease the delay in each run
     */
    private int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN = 30;

    /**
     * Is the minimum delay time in seconds a tour must have so a delay problem will be created
     * If you change this, you have to change a value in frontend as well
     */
    private int LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM = 2 * 60;

    /**
     * Setting this to false turns the live engine off
     */
    private boolean LIVE_ENGINE_RUN_LIVE = true;


    private Logger logger = Logger.getLogger(LiveEngine.class);

    private HashMap<String, List<TimedVehicleProblem>> newVehicleProblems; //maps from vehicle id to a list of vehicle problems
    private HashMap<String, List<TimedStopProblem>> newStopProblems; //maps from stop id to a list of stop problems
    private HashMap<String, List<TimedLineProblem>> newLineProblems; //maps from line id to a list of line problems
    private long lastCallOfNewFeedbacksMethod = new Date().getTime(); //initialize it to "now"

    /* Here we store all the problems the live engine may delete randomly.
       Only problems that can be solved externally (like an overflow at a stop) can be deleted automatically.
       For other types of problems the smart city operator has to do something, e.g. create a service request
       for a broken wheel in a vehicle. */
    private HashSet<VehicleProblem> vehicleProblemsThatCanBeDeleted = VehicleProblem.getDeletableProblems();
    private HashSet<StopProblem> stopProblemsThatCanBeDeleted = StopProblem.getDeletableProblems();
    private HashSet<LineProblem> lineProblemsThatCanBeDeleted = LineProblem.getDeletableProblems();

    //maps from a vehicle id to a Delay object containing the expected and actual position as well as the delay in seconds
    private HashMap<String, Delay> vehicleDelays;

    private LiveEngineCreativity liveEngineCreativity; //we need this for predefined customer feedback strings
    private Timer timer; //timer task for calling live engine repeatedly and regularly

    /**
     * This method is the heart of the live engine, i.e. the data generation logic.
     * It simulates part of the "outside world".
     * It generates and deletes random problems for vehicles, stops and lines according to the
     * live engine configuration variables.
     * The live engine calls an update method on the fleet resp. the network so they can update
     * their vehicles, stops and lines. Since the fleet resp. the network now have all the information
     * about the problems, the live engine may delete its stored problems.
     * Additionally this method creates random customer feedbacks and delays for the vehicles.
     * This method is called repeatedly since LiveEngineImpl extends TimerTask.
     */
    @Override
    public void run() {
        if (!this.LIVE_ENGINE_RUN_LIVE) { //if the live engine should not run, we do not do anything
            return;
        }

        if (this.newVehicleProblems == null || this.newStopProblems == null || this.newLineProblems == null ||
                this.vehicleDelays == null) { //in the beginning
            initializeAttributes();
        }

        updateDelays(); // internal delay map
        generateProblems(); //the live engine generates some ...
        deleteProblems(); //... and deletes other problems
        updateDelayProblems(); // update delay problem external and internal
        generateFeedbacks(); //we also generate customer feedbacks
    }

    /**
     * This method generates random customer feedbacks and saves them to the database
     */
    void generateFeedbacks() {
        Network network = ServiceRegistry.getService(Network.class);
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);

        Random random = new Random();

        for (int i = 0; i < LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN; i++) {
            FeedbackEnum feedbackType = FeedbackEnum.values()[random.nextInt(FeedbackEnum.values().length)]; //pick a random feedback type
            Feedback feedback = null;
            int randomProblemInt = 0;
            switch (feedbackType) {
                //depending on the feedback type (line, vehicle, stop or general) we create different types of feedback objects
                case Line:
                    List<Line> lines = network.getLines();
                    randomProblemInt = random.nextInt(LineProblem.values().length); //a line feedback is matched to a line problem
                    feedback = new Feedback(feedbackService.getFreshFeedbackID(),
                            this.liveEngineCreativity.getRandomProblem(feedbackType, randomProblemInt), new Date(), false,
                            LineProblem.getLineProblemByNumber(randomProblemInt).toString(),
                            feedbackType, lines.get(random.nextInt(lines.size())).getId()); //we reference a random but existing line in this feedback
                    break;
                case Vehicle:
                    List<Vehicle> vehicles = fleetService.getAllVehicles();
                    //Delay and Defect_Wheel can not be generated here (therefore -2)
                    randomProblemInt = random.nextInt(VehicleProblem.values().length - 2);
                    feedback = new Feedback(feedbackService.getFreshFeedbackID(),
                            this.liveEngineCreativity.getRandomProblem(feedbackType, randomProblemInt), new Date(), false,
                            VehicleProblem.getVehicleProblemByNumber(randomProblemInt).toString(),
                            feedbackType, vehicles.get(random.nextInt(vehicles.size())).getId());
                    break;
                case Stop:
                    List<Stop> stops = network.getStops();
                    randomProblemInt = random.nextInt(StopProblem.values().length);
                    feedback = new Feedback(feedbackService.getFreshFeedbackID(),
                            this.liveEngineCreativity.getRandomProblem(feedbackType, randomProblemInt), new Date(), false,
                            StopProblem.getStopProblemByNumber(randomProblemInt).toString(),
                            feedbackType, stops.get(random.nextInt(stops.size())).getId());
                    break;
                case General:
                    feedback = new Feedback(feedbackService.getFreshFeedbackID(),
                            this.liveEngineCreativity.getRandomProblem(feedbackType, 0), new Date(), false, "",
                            feedbackType, "");
                    break;
                default:
                    this.logger.warn("Unknown feedback type while generating feedbacks");
            }

            feedbackService.saveFeedback(feedback);
        }

        this.logger.info("Generated " + LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN + " feedbacks.");
    }

    /**
     * This method generates random problems for vehicles, stops and lines according to the
     * live engine configuration variables. It also updates the data in the fleet and network services.
     */
    void generateProblems() {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        generateVehicleProblems(LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN);
        generateStopProblems(LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN);
        generateLineProblems(LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN);

        this.logger.info("Finished generating random problems. " +
                "Total vehicle problems: " + computeNumberOfVehicleProblems() + ", " +
                "total stop problems: " + computeNumberOfStopProblems() + ", " +
                "total line problems: " + computeNumberOfLineProblems());
    }

    /**
     * This method deletes random problems for vehicles, stops and lines according to the
     * live engine configuration variables. It also updates the data in the fleet and network services.
     * @return an array of the amount of deleted vehicle, stop and line problems.
     */
    int[] deleteProblems() {
        int deletedVehicleProblems = deleteVehicleProblems(LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN);
        int deletedStopProblems = deleteStopProblems(LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN);
        int deletedLineProblems = deleteLineProblems(LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN);

        this.logger.info("Finished deleting random problems. " +
                "Total vehicle problems: " + computeNumberOfVehicleProblems() + ", " +
                "total stop problems: " + computeNumberOfStopProblems() + ", " +
                "total line problems: " + computeNumberOfLineProblems());

        return new int[]{deletedVehicleProblems, deletedStopProblems, deletedLineProblems};
    }

    /**
     * Transfers all new vehicle, stop and line problems to the fleet and network services.
     */
    void flushDataToServices() {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        enrichVehicles(fleetService.getAllVehicles());
        enrichStops(network.getStops());
        enrichLines(network.getLines());
    }


    /**
     * Creates and initializes services and Hash maps
     */
    @Override
    public void initialize() {
        this.logger.info("Initializing live engine");

        this.newVehicleProblems = new HashMap<>();
        this.newStopProblems = new HashMap<>();
        this.newLineProblems = new HashMap<>();
        this.vehicleDelays = new HashMap<>();
        this.liveEngineCreativity = new LiveEngineCreativity();
    }

    /**
     * Starts the mechanism that the live engine runs every LIVE_ENGINE_FREQUENCY_SECONDS seconds and
     * generates initial problems according to the configuration in the LIVE_ENGINE_STARTING_... variables
     */
    @Override
    public void start() { //at this point in time all services should be initialized so we can add them here
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        initializeAttributes();

        generateVehicleProblems((int) Math.round(fleetService.getAllVehicles().size() *
                ((double) LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE) / 100));
        generateStopProblems((int) Math.round(network.getStops().size() *
                ((double) LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE) / 100));
        generateLineProblems((int) Math.round(network.getLines().size() *
                ((double) LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE) / 100));

        updateVehiclesDelayList();
        generateDelayProblems((int) Math.round(this.vehicleDelays.size() *
                ((double) LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE) / 100));

        this.logger.info("Started live engine with " + computeNumberOfVehicleProblems() +
                " vehicle problems, " + computeNumberOfStopProblems() + " stop problems and " +
                computeNumberOfLineProblems() + " line problems.");

        //live engine generates vehicle, stop and line problems every LIVE_ENGINE_FREQUENCY_SECONDS seconds
        this.timer = new Timer();
        this.timer.schedule(this, 0, LIVE_ENGINE_FREQUENCY_SECONDS * 1000);
    }

    @Override
    public void restart() {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        updateVehiclesDelayList();
        initializeAttributes(); //kill all problems that are local to live engine

        //set delay time to 0 for all vehicles in live engine
        for (String vehicleWithDelay : this.vehicleDelays.keySet()) {
            this.vehicleDelays.get(vehicleWithDelay).setDelay(0);
        }

        //clear all problems and delays in the fleet and network
        for (Vehicle vehicle : fleetService.getAllVehicles()) {
            vehicle.setProblems(new ArrayList<>());
            vehicle.setDelayTimeInSeconds(0);
        }

        for (Stop stop : network.getStops()) {
            stop.setProblems(new ArrayList<>());
        }

        for (Line line : network.getLines()) {
            line.setProblems(new ArrayList<>());
        }

        timer.cancel(); //kill the old timer
        timer.purge();

        ServiceRegistry.restartLiveEngine(); //we have to create a new LiveEngine object so we can reschedule the timer task
        LiveEngine newLiveEngine = ServiceRegistry.getService(LiveEngine.class);
        this.transferVariablesFromFrontendConfiguredLiveEngineToNewLiveEngine(newLiveEngine); //get all the variables the frontend tells us about
        ServiceRegistry.getService(LiveEngine.class).start(); //and start the live engine again
    }

    @Override
    public boolean isLiveEngineStarted() {
        return this.timer != null;
    }

    /**
     * Computes the number of vehicle problems the live engine currently knows about. Used for logging reasons.
     * @return the number of vehicle problems in the live engine
     */
    int computeNumberOfVehicleProblems() {
        int amountOfVehicleProblems = 0;
        for (List<TimedVehicleProblem> vehicleProblems : this.getNewVehicleProblems().values()) {
            amountOfVehicleProblems += vehicleProblems.size();
        }

        return amountOfVehicleProblems;
    }

    /**
     * Computes the number of stop problems the live engine currently knows about. Used for logging reasons.
     * @return the number of stop problems in the live engine
     */
    int computeNumberOfStopProblems() {
        int amountOfStopProblems = 0;
        for (List<TimedStopProblem> vehicleProblems : this.getNewStopProblems().values()) {
            amountOfStopProblems += vehicleProblems.size();
        }

        return amountOfStopProblems;
    }

    /**
     * Computes the number of line problems the live engine currently knows about. Used for logging reasons.
     * @return the number of line problems in the live engine
     */
    int computeNumberOfLineProblems() {
        int amountOfLineProblems = 0;
        for (List<TimedLineProblem> lineProblems : this.getNewLineProblems().values()) {
            amountOfLineProblems += lineProblems.size();
        }

        return amountOfLineProblems;
    }

    /**
     * Fills hash maps (containing the problems) that the live engine needs
     */
    private void initializeAttributes() {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        for (Vehicle vehicle : fleetService.getAllVehicles()) {
            this.newVehicleProblems.put(vehicle.getId(), new ArrayList<>()); //no problem yet
        }
        //lines before stops to avoid that the enrichStops() call in network.getLines() resets everything
        for (Line line : network.getLines()) {
            this.newLineProblems.put(line.getId(), new ArrayList<>()); //no problem yet
        }
        for (Stop stop : network.getStops()) {
            this.newStopProblems.put(stop.getId(), new ArrayList<>()); //no problem yet
        }
    }

    void generateVehicleProblem() {
        generateVehicleProblems(1);
    }

    void generateStopProblem() {
        generateStopProblems(1);
    }

    void generateLineProblem() {
        generateLineProblems(1);
    }

    @Override
    public void generateVehicleProblems(int amount) {
        if (this.newVehicleProblems.isEmpty()) { //if there are no vehicles we cannot generate a problem for a vehicle
            this.logger.info("No vehicle problems to fill");
            return;
        }
        Object[] keys = this.newVehicleProblems.keySet().toArray(); //keys contains all vehicle IDs

        for (int i = 0; i < amount; i++) { //generate as many vehicle problems as requested
            int randomVehicleIndex = new Random().nextInt(this.newVehicleProblems.size());
            String key = (String) keys[randomVehicleIndex]; //we choose a random vehicle to which we want to assign this problem
            VehicleProblem vehicleProblem = VehicleProblem.getRandomVehicleProblem();
            addVehicleProblem(key, vehicleProblem); //add the randomly generated vehicle problem to the randomly chosen vehicle
        }
    }

    @Override
    public void generateStopProblems(int amount) {
        if (this.newStopProblems.isEmpty()) { //if there are no stops we cannot generate a problem for a stop
            this.logger.info("No stop problems to fill");
            return;
        }
        Object[] keys = this.newStopProblems.keySet().toArray(); //keys contains all stop IDs

        for (int i = 0; i < amount; i++) {
            int randomStopIndex = new Random().nextInt(this.newStopProblems.size());
            String key = (String) keys[randomStopIndex]; //we choose a random stop to which we want to assign this problem
            StopProblem stopProblem = StopProblem.getRandomStopProblem();
            addStopProblem(key, stopProblem); //add the randomly generated stop problem to the randomly chosen stop
        }
    }

    @Override
    public void generateLineProblems(int amount) {
        if (this.newLineProblems.isEmpty()) { //if there are no lines we cannot generate a problem for a line
            this.logger.info("No line problems to fill");
            return;
        }
        Object[] keys = this.newLineProblems.keySet().toArray(); //keys contains all line IDs

        for (int i = 0; i < amount; i++) {
            int randomLineIndex = new Random().nextInt(this.newLineProblems.size());
            String key = (String) keys[randomLineIndex]; //we choose a random line to which we want to assign this problem
            LineProblem lineProblem = LineProblem.getRandomLineProblem();
            addLineProblem(key, lineProblem); //add the randomly generated line problem to the randomly chosen line
        }
    }

    /**
     * Helper method for creating a given amount of Delay at the beginning
     * Should only be called at the beginning to create delay problems and not during run time
     * @param amount = number of delay problems which should be created
     */
    private void generateDelayProblems(int amount) {
        if (this.vehicleDelays.isEmpty()) {
            this.logger.info("No vehicle can have delays at the moment because no vehicle is driving");
            return;
        }
        if (this.vehicleDelays.size() < amount) {
            amount = this.vehicleDelays.size();
        }

        List<String> keys = new ArrayList<>();
        keys.addAll(this.vehicleDelays.keySet());

        for (int i = 0; i < amount; i++) { //generate as many delay problems as requested or are as possible
            int randomDelayIndex = new Random().nextInt(keys.size());
            String key = keys.get(randomDelayIndex);

            createVehicleDelayProblemFor(key);

            keys.remove(randomDelayIndex); // remove key since a vehicle can not have multiple delay problems
            // set delay between 2 und 4 minutes
            vehicleDelays.get(key).setDelay( 2 * 60 + new Random().nextInt(2 * 60));
        }
    }

    /**
     * Helper method for generateDelayProblems(int amount) and updateDelayProblems().
     * Creates a delay problem for the vehicle with the id vehicleId
     * vehicleId must be in the vehicleDelays map otherwise it will not be created
     * @param vehicleId id of the vehicle which should get a delay problem
     */
    private void createVehicleDelayProblemFor(String vehicleId) {
        if (this.vehicleDelays.get(vehicleId) == null) {
            return;
        }

        if (this.newVehicleProblems.containsKey(vehicleId)) {
            List<TimedVehicleProblem> vehicleProblems = this.newVehicleProblems.get(vehicleId);
            vehicleProblems.add(new TimedVehicleProblem(VehicleProblem.Delay)); //add a new Delay problem
        } else {
            List<TimedVehicleProblem> newProblems = new ArrayList<>();
            newProblems.add(new TimedVehicleProblem(VehicleProblem.Delay));
            this.newVehicleProblems.put(vehicleId, newProblems); //add a new problems list with a single Delay problem
        }
    }

    /**
     * Helper function, called in generateVehicleProblem()
     * Updates this.newVehicleProblems
     * @param vehicleID      the vehicle to which the problem should be added
     * @param vehicleProblem the problem that should be added to the vehicle
     */
    private void addVehicleProblem(String vehicleID, VehicleProblem vehicleProblem) {
        if (this.newVehicleProblems.containsKey(vehicleID)) { //vehicle already exists in our internal vehicle -> problems map
            List<TimedVehicleProblem> vehicleProblems = this.newVehicleProblems.get(vehicleID);
            vehicleProblems.add(new TimedVehicleProblem(vehicleProblem));
        } else {
            List<TimedVehicleProblem> newProblems = new ArrayList<>();
            newProblems.add(new TimedVehicleProblem(vehicleProblem));
            this.newVehicleProblems.put(vehicleID, newProblems); //if the vehicle does not exist in our map yet, we put it there
        }
    }

    /**
     * Helper function, called in generateStopProblem()
     * Updates this.newStopProblems
     * @param stopID      the stop to which the problem should be added
     * @param stopProblem the problem that should be added to the stop
     */
    private void addStopProblem(String stopID, StopProblem stopProblem) {
        if (this.newStopProblems.containsKey(stopID)) { //stop already exists in our internal stop -> problems map
            List<TimedStopProblem> stopProblems = this.newStopProblems.get(stopID);
            stopProblems.add(new TimedStopProblem(stopProblem));
        } else {
            List<TimedStopProblem> newProblems = new ArrayList<>();
            newProblems.add(new TimedStopProblem(stopProblem));
            this.newStopProblems.put(stopID, newProblems); //if the stop does not exist in our map yet, we put it there
        }
    }

    /**
     * Helper function, called in generateLineProblem()
     * Updates this.newLineProblems
     * @param lineID      the line to which the problem should be added
     * @param lineProblem the problem that should be added to the line
     */
    private void addLineProblem(String lineID, LineProblem lineProblem) {
        if (this.newLineProblems.containsKey(lineID)) { //line already exists in our internal line -> problems map
            List<TimedLineProblem> lineProblems = this.newLineProblems.get(lineID);
            lineProblems.add(new TimedLineProblem(lineProblem));
        } else {
            List<TimedLineProblem> newProblems = new ArrayList<>();
            newProblems.add(new TimedLineProblem(lineProblem));
            this.newLineProblems.put(lineID, newProblems); //if the line does not exist in our map yet, we put it there
        }
    }

    @Override
    public void enrichVehicles(Collection<Vehicle> vehicles) {
        if (this.newVehicleProblems == null) {
            this.initialize();
        }
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        HashMap<String, List<TimedVehicleProblem>> problemsToAdd = new HashMap<>();
        for (Vehicle vehicle : vehicles) { //for all vehicles that are to be enriched we check if we have it in our internal problems map
            if (this.newVehicleProblems.containsKey(vehicle.getId())) {
                problemsToAdd.put(vehicle.getId(), new ArrayList<>());
                for (TimedVehicleProblem problem : this.newVehicleProblems.get(vehicle.getId())) {
                    //add all problems from our internal problems map so we can give it to the fleet later on
                    problemsToAdd.get(vehicle.getId()).add(problem);
                }
            }
        }

        HashMap<String, Delay> vehiclePositionsWithDelays = generateDelays(vehicles); //also generate delays for the passed vehicles
        fleetService.updateVehicles(problemsToAdd, vehiclePositionsWithDelays); //return the live data, i.e. problems and positions with delays, to the fleet

        for (String vehicle : this.newVehicleProblems.keySet()) {
            this.newVehicleProblems.put(vehicle, new ArrayList<>()); //clear all vehicle problems internally -> those are now the fleet's business
        }
    }

    @Override
    public void enrichStops(Collection<Stop> stops) {
        if (this.newStopProblems == null) {
            this.initialize();
        }
        Network network = ServiceRegistry.getService(Network.class);

        HashMap<String, List<TimedStopProblem>> problemsToAdd = new HashMap<>();
        for (Stop stop : stops) { //for all stops that are to be enriched we check if we have it in our internal problems map
            if (this.newStopProblems.containsKey(stop.getId())) {
                problemsToAdd.put(stop.getId(), new ArrayList<>());
                for (TimedStopProblem problem : this.newStopProblems.get(stop.getId())) {
                    //add all problems from our internal problems map so we can give it to the network later on
                    problemsToAdd.get(stop.getId()).add(problem);
                }
                // delete problems since they are now handled by the networkService
                this.newStopProblems.get(stop.getId()).clear();
            }
        }

        network.updateStops(problemsToAdd);
    }

    @Override
    public void enrichLines(List<Line> lines) {
        if (this.newLineProblems == null) {
            this.initialize();
        }
        Network network = ServiceRegistry.getService(Network.class);

        HashMap<String, List<TimedLineProblem>> problemsToAdd = new HashMap<>();
        for (Line line : lines) { //for all lines that are to be enriched we check if we have it in our internal problems map
            if (this.newLineProblems.containsKey(line.getId())) {
                problemsToAdd.put(line.getId(), new ArrayList<>());
                for (TimedLineProblem problem : this.newLineProblems.get(line.getId())) {
                    //add all problems from our internal problems map so we can give it to the network later on
                    problemsToAdd.get(line.getId()).add(problem);
                }
                // delete problems since they are now handled by the networkService
                this.newLineProblems.get(line.getId()).clear();
            }
        }

        network.updateLines(problemsToAdd);
    }


    @Override
    public int deleteVehicleProblems(int amount) {
        if (this.newVehicleProblems.isEmpty()) {
            this.logger.info("No vehicle problems to delete");
        }

        List<List<TimedVehicleProblem>> nonEmptyVehicleProblems = new ArrayList<>();
        int availableProblems = buildListOfNonEmptyVehicleProblems(nonEmptyVehicleProblems);

        /* We cast TimedVehicleProblem to TimedProblem because there is a general method that does the problem deletion
           for vehicles, stops and lines */
        List<List<TimedProblem>> nonEmptyProblems =
                (List<List<TimedProblem>>) (List<? extends List<? extends TimedProblem>>) nonEmptyVehicleProblems;
        return executeProblemDeletion(amount, nonEmptyProblems, availableProblems, "vehicle");
    }

    @Override
    public int deleteStopProblems(int amount) {
        if (this.newStopProblems.isEmpty()) {
            this.logger.info("No stop problems to delete");
        }

        List<List<TimedStopProblem>> nonEmptyStopProblems = new ArrayList<>();
        int availableProblems = buildListOfNonEmptyStopProblems(nonEmptyStopProblems);

        /* We cast TimedStopProblem to TimedProblem because there is a general method that does the problem deletion
           for vehicles, stops and lines */
        List<List<TimedProblem>> nonEmptyProblems =
                (List<List<TimedProblem>>) (List<? extends List<? extends TimedProblem>>) nonEmptyStopProblems;
        return executeProblemDeletion(amount, nonEmptyProblems, availableProblems, "stop");
    }

    @Override
    public int deleteLineProblems(int amount) {
        if (this.newLineProblems.isEmpty()) {
            this.logger.info("No line problems to delete");
        }

        List<List<TimedLineProblem>> nonEmptyLineProblems = new ArrayList<>();
        int availableProblems = buildListOfNonEmptyLineProblems(nonEmptyLineProblems);

        /* We cast TimedLineProblem to TimedProblem because there is a general method that does the problem deletion
           for vehicles, stops and lines */
        List<List<TimedProblem>> nonEmptyProblems =
                (List<List<TimedProblem>>) (List<? extends List<? extends TimedProblem>>) nonEmptyLineProblems;
        return executeProblemDeletion(amount, nonEmptyProblems, availableProblems, "line");
    }

    /**
     * Executes the actual problem deletion for vehicles, stops and lines
     * @param amount the amount of problems we want to delete
     * @param nonEmptyProblems a list of lists containing TimedProblems that can be deleted
     * @param availableProblems the amount of problems that are available for deletion
     * @param type a string indicating whether we want to delete vehicle, stop or line problems
     * @return the amount of problems that were actually deleted
     */
    private int executeProblemDeletion(int amount, List<List<TimedProblem>> nonEmptyProblems,
                                       int availableProblems, String type) {
        int deletedProblems = 0;
        while (availableProblems > 0 && deletedProblems < amount) {
            List<TimedProblem> listFromWhichWeDeletedProblem = removeRandomProblemFromList(nonEmptyProblems); //remove the problem
            //update meta counting variables
            deletedProblems++;
            availableProblems--;
            updateNonEmptyProblemsAfterDeleting(nonEmptyProblems, listFromWhichWeDeletedProblem); //update the list of deletable problems
        }

        return printLiveEngineDeletedMessage(amount, deletedProblems, type); //log what happened
    }

    /**
     * Picks a random problem from a list of random problem lists and removes it.
     * @param nonEmptyProblems a list of a list of TimedProblems (because we have a list of TimedProblems for each vehicle, stop and line)
     * @return the list of TimedProblems from which we removed the randomly picked problem
     */
    private List<TimedProblem> removeRandomProblemFromList(List<List<TimedProblem>> nonEmptyProblems) {
        int randomListIndex = new Random().nextInt(nonEmptyProblems.size());
        List<TimedProblem> listToDeleteProblemFrom = nonEmptyProblems.get(randomListIndex); //first pick a random list of problems to remove from
        int randomIndexInList = new Random().nextInt(listToDeleteProblemFrom.size());
        TimedProblem problem = listToDeleteProblemFrom.get(randomIndexInList); //then pick a random problem from that list

        if (problem instanceof TimedVehicleProblem) {
            while (!this.vehicleProblemsThatCanBeDeleted.contains( //find a problem that may be deleted (at least one must exist)
                    ((TimedVehicleProblem) listToDeleteProblemFrom.get(randomIndexInList)).getVehicleProblem())) {
                randomIndexInList = updateRandomIndex(listToDeleteProblemFrom.size(), randomIndexInList);
            }
        }

        if (problem instanceof TimedStopProblem) {
            while (!this.stopProblemsThatCanBeDeleted.contains( //find a problem that may be deleted (at least one must exist)
                    ((TimedStopProblem) listToDeleteProblemFrom.get(randomIndexInList)).getStopProblem())) {
                randomIndexInList = updateRandomIndex(listToDeleteProblemFrom.size(), randomIndexInList);
            }
        }

        if (problem instanceof TimedLineProblem) {
            while (!this.lineProblemsThatCanBeDeleted.contains( //find a problem that may be deleted (at least one must exist)
                    ((TimedLineProblem) listToDeleteProblemFrom.get(randomIndexInList)).getLineProblem())) {
                randomIndexInList = updateRandomIndex(listToDeleteProblemFrom.size(), randomIndexInList);
            }
        }

        listToDeleteProblemFrom.remove(randomIndexInList); //actually remove the problem ...
        return listToDeleteProblemFrom; //... and return the list
    }

    /**
     * After a problem has been deleted we need to update our list of deletable problems
     * @param nonEmptyProblems the big list containing lists of TimedProblems
     * @param listFromWhichWeDeletedProblem a single list of TimedProblems from which we just deleted a problem
     */
    private void updateNonEmptyProblemsAfterDeleting(List<List<TimedProblem>> nonEmptyProblems,
                                                     List<TimedProblem> listFromWhichWeDeletedProblem) {
        boolean thereIsStillAProblemToDeleteLeftInOurList = false;
        for (TimedProblem problem : listFromWhichWeDeletedProblem) {
            if (problem instanceof TimedVehicleProblem && //check for all other problems in that list if they are deletable
                    this.vehicleProblemsThatCanBeDeleted.contains(((TimedVehicleProblem) problem).getVehicleProblem())) {
                thereIsStillAProblemToDeleteLeftInOurList = true;
                break;
            }

            if (problem instanceof TimedStopProblem && //check for all other problems in that list if they are deletable
                    this.stopProblemsThatCanBeDeleted.contains(((TimedStopProblem) problem).getStopProblem())) {
                thereIsStillAProblemToDeleteLeftInOurList = true;
                break;
            }

            if (problem instanceof TimedLineProblem && //check for all other problems in that list if they are deletable
                    this.lineProblemsThatCanBeDeleted.contains(((TimedLineProblem) problem).getLineProblem())) {
                thereIsStillAProblemToDeleteLeftInOurList = true;
                break;
            }
        }

        if (!thereIsStillAProblemToDeleteLeftInOurList) { //if there is no deletable problem left -> remove it from the nonempty lists
            nonEmptyProblems.remove(listFromWhichWeDeletedProblem);
        }
    }

    /**
     * Prints information about the deletion of problems by the live engine
     * @param amount          the amount of problems that we attempted to delete
     * @param deletedProblems the amount of problems that were actually deleted
     * @param type            the type of problems that were deleted ("vehicle", "stop" or "line")
     * @return the amount of problems that were actually deleted
     */
    private int printLiveEngineDeletedMessage(int amount, int deletedProblems, String type) {
        this.logger.info("Live engine deleted " + deletedProblems + " " + type + " problems." +
                (deletedProblems < amount ? " Attempted to delete " + amount + " problems but " +
                        "there were only " + deletedProblems + " available." : ""));
        return deletedProblems;
    }


    /**
     * This helper method determines the list of all vehicle problems for every vehicle in the fleet that
     * contains a deletable problem in its problems list.
     * We need this so the live engine knows how many and which problems can be randomly deleted.
     * It may not delete all problems randomly (see e.g. comment in VehicleProblem.getDeletableProblems()).
     * @param nonEmptyVehicleProblems an empty list which we fill with a list of all vehicle problems for each vehicle
     *                                (if that list contains deletable problems) -> that is why it is a list of lists
     * @return the amount of deletable vehicle problems
     */
    private int buildListOfNonEmptyVehicleProblems(List<List<TimedVehicleProblem>> nonEmptyVehicleProblems) {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);

        int availableProblems = 0;
        List<Vehicle> vehicles = fleetService.getAllVehicles();
        for (Vehicle vehicle : vehicles) {
            //iterate over all vehicles...
            List<TimedVehicleProblem> vehicleProblems = vehicle.getProblems();
            if (vehicleProblems == null) {
                continue;
            }
            for (TimedVehicleProblem timedVehicleProblem : vehicleProblems) {
                //...to find a vehicleProblem...
                for (VehicleProblem vehicleProblemThatCanBeDeleted : this.vehicleProblemsThatCanBeDeleted) {
                    //...that may be deleted randomly by the live engine
                    if (timedVehicleProblem.getVehicleProblem().equals(vehicleProblemThatCanBeDeleted)) {
                        //the live engine may delete this vehicle problem randomly (it may not delete all vehicle problems)
                        if (!nonEmptyVehicleProblems.contains(vehicleProblems)) {
                            //we keep track of this list of vehicle problems so we can remember it when choosing a random problem to delete later on
                            nonEmptyVehicleProblems.add(vehicleProblems);
                        }
                        availableProblems++;
                    }
                }
            }
        }

        return availableProblems;
    }

    /**
     * This helper method determines the list of all stop problems for every stop in the network that
     * contains a deletable problem in its problems list.
     * We need this so the live engine knows how many and which problems can be randomly deleted.
     * It may not delete all problems randomly (see e.g. comment in StopProblem.getDeletableProblems()).
     * @param nonEmptyStopProblems an empty list which we fill with a list of all stop problems for each stop
     *                                (if that list contains deletable problems) -> that is why it is a list of lists
     * @return the amount of deletable stop problems
     */
    private int buildListOfNonEmptyStopProblems(List<List<TimedStopProblem>> nonEmptyStopProblems) {
        Network network = ServiceRegistry.getService(Network.class);

        int availableProblems = 0;
        List<Stop> stops = network.getStops();
        for (Stop stop : stops) {
            //iterate over all stops...
            List<TimedStopProblem> stopProblems = stop.getProblems();
            if (stopProblems == null) {
                continue;
            }
            for (TimedStopProblem timedStopProblem : stopProblems) {
                //...to find a stopProblem...
                for (StopProblem stopProblemThatCanBeDeleted : this.stopProblemsThatCanBeDeleted) {
                    //...that may be deleted randomly by the live engine
                    if (timedStopProblem.getStopProblem().equals(stopProblemThatCanBeDeleted)) {
                        //the live engine may delete this stop problem randomly (it may not delete all stop problems)
                        if (!nonEmptyStopProblems.contains(stopProblems)) {
                            //we keep track of this list of stop problems so we can remember it when choosing a random problem to delete later on
                            nonEmptyStopProblems.add(stopProblems);
                        }
                        availableProblems++;
                    }
                }
            }
        }

        return availableProblems;
    }

    /**
     * This helper method determines the list of all line problems for every line in the network that
     * contains a deletable problem in its problems list.
     * We need this so the live engine knows how many and which problems can be randomly deleted.
     * It may not delete all problems randomly (see e.g. comment in LineProblem.getDeletableProblems()).
     * @param nonEmptyLineProblems an empty list which we fill with a list of all line problems for each line
     *                                (if that list contains deletable problems) -> that is why it is a list of lists
     * @return the amount of deletable line problems
     */
    private int buildListOfNonEmptyLineProblems(List<List<TimedLineProblem>> nonEmptyLineProblems) {
        Network network = ServiceRegistry.getService(Network.class);

        int availableProblems = 0;
        List<Line> lines = network.getLines();
        for (Line line : lines) {
            //iterate over all lines...
            List<TimedLineProblem> lineProblems = line.getProblems();
            if (lineProblems == null) {
                continue;
            }
            for (TimedLineProblem timedLineProblem : lineProblems) {
                //...to find a lineProblem...
                for (LineProblem lineProblemThatCanBeDeleted : this.lineProblemsThatCanBeDeleted) {
                    //...that may be deleted randomly by the live engine
                    if (timedLineProblem.getLineProblem().equals(lineProblemThatCanBeDeleted)) {
                        //the live engine may delete this line problem randomly (it may not delete all line problems)
                        if (!nonEmptyLineProblems.contains(lineProblems)) {
                            //we keep track of this list of line problems so we can remember it when choosing a random problem to delete later on
                            nonEmptyLineProblems.add(lineProblems);
                        }
                        availableProblems++;
                    }
                }
            }
        }

        return availableProblems;
    }

    /**
     * Picks a new random index in a list. Used for finding a deletable problem in a list
     * @param sizeOfListToDeleteProblemFrom the size of the list from which we want to delete a problem from
     * @param randomIndexInList the last index that we tried in the list
     * @return the new index
     */
    private int updateRandomIndex(int sizeOfListToDeleteProblemFrom, int randomIndexInList) {
        if (randomIndexInList == sizeOfListToDeleteProblemFrom - 1) {
            randomIndexInList = 0;
        } else {
            randomIndexInList++;
        }

        return randomIndexInList;
    }

    @Override
    public HashMap<String, Position> generatePositions(Collection<Vehicle> vehicles) {
        TourStore tourStore = ServiceRegistry.getService(TourStore.class);

        HashMap<String, Position> positions = new HashMap<>(vehicles.size());
        Iterator<Vehicle> vehicleIterator = vehicles.iterator(); // use iterator to avoid ConcurrentModificationException
        while (vehicleIterator.hasNext()) {
            Vehicle vehicle = vehicleIterator.next();
            Tour tour = tourStore.getCurrentTourForVehicle(vehicle.getId());
            if (tour == null) { //vehicle is currently not driving on a tour
                positions.put(vehicle.getId(), null);
                continue;
            }

            vehicle.setActualTour(tour);
            Position position = tourStore.getPositionFromTour(tour);
            positions.put(vehicle.getId(), position);
        }

        return positions;
    }

    @Override
    public HashMap<String, Delay> generateDelays(Collection<Vehicle> vehicles) {
        TourStore tourStore = ServiceRegistry.getService(TourStore.class);

        HashMap<String, Position> expectedPositions = generatePositions(vehicles);
        HashMap<String, Delay> notCurrentRunningVehicleDelays = new HashMap<>();

        for (String vehicleID : expectedPositions.keySet()) {
            Position expectedPosition = expectedPositions.get(vehicleID);
            Tour currentTour = tourStore.getCurrentTourForVehicle(vehicleID);
            updateVehiclesDelayList(); // update the list so it definitely contains all currently running tours
            Delay vehicleDelay = vehicleDelays.get(vehicleID);

            if (currentTour == null ) { //if the vehicle is currently not driving, we cannot return anything useful
                //this.logger.warn("Vehicle with ID " + vehicleID + " is currently not driving (tour is null)");
                notCurrentRunningVehicleDelays.put(vehicleID, new Delay(null, null, 0));
                continue;
            }

            Position actualPosition = null;
            if (vehicleDelay != null) {
                actualPosition = tourStore.getPositionFromDelayedTour(currentTour, vehicleDelay.getDelay());
            }
            //this.logger.info("Vehicle with ID " + vehicleID + " try to calculate actual position");
            //this.logger.info("Delay list: " + vehicleDelays.keySet().toString() + " ");
            if (vehicleDelays.get(vehicleID) == null) {
                continue; // this should normally never happen
            }
            vehicleDelays.get(vehicleID).setActualPosition(actualPosition);
            vehicleDelays.get(vehicleID).setExpectedPosition(expectedPosition);
        }
        HashMap<String, Delay> resultMap = new HashMap<>();
        resultMap.putAll(notCurrentRunningVehicleDelays);
        resultMap.putAll(vehicleDelays);

        return resultMap;
    }

    private void updateDelayProblems() {
        for (String vehicleID : vehicleDelays.keySet()) {
            Delay delay = vehicleDelays.get(vehicleID);
            if (delay.getDelay() >= LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM) {

                // check if the vehicle already has a Delay Problem
                FleetService fleetService = ServiceRegistry.getService(FleetService.class);
                Vehicle vehicle = fleetService.getVehicleForId(vehicleID);
                boolean problemAlreadyExists = false;
                if (vehicle.getProblems() != null) {
                    for (TimedVehicleProblem timedVehicleProblem : vehicle.getProblems()) {
                        if (timedVehicleProblem.getVehicleProblem() == VehicleProblem.Delay) {
                            problemAlreadyExists = true;
                        }
                    }
                }
                if (problemAlreadyExists) {
                    continue; // do not add the the Delay when it already exists -> avoids duplicate Delay problems
                }

                // check if there is already Delay Problem for the vehicle but was not yet enriched
                if (this.newVehicleProblems.containsKey(vehicleID)) {
                    List<TimedVehicleProblem> vehicleProblems = this.newVehicleProblems.get(vehicleID);
                    for (TimedVehicleProblem timedVehicleProblem : vehicleProblems) {
                        if (timedVehicleProblem.getVehicleProblem() == VehicleProblem.Delay) {
                            problemAlreadyExists = true;
                        }
                    }

                    if (problemAlreadyExists) {
                        continue; // do not add the the Delay when it already exists
                    }
                }

                createVehicleDelayProblemFor(vehicleID);
            }
        }
        // delete all problems were the delay got less than LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM
        deleteNotUpToDateDelayProblems();
    }

    /**
     * Helper method for updateDelayProblems
     * It deletes all Delay Problems which are not "correct" any more
     * (where delay < LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM)
     */
    private void deleteNotUpToDateDelayProblems() {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        List<Vehicle> vehicles = fleetService.getAllVehicles();

        for (Vehicle vehicle : vehicles) {
            //iterate over all vehicles...
            List<TimedVehicleProblem> vehicleProblems = vehicle.getProblems();
            if (vehicleProblems == null) {
                continue;
            }
            TimedVehicleProblem toRemoveTimedVehicleProblem = null;
            for (TimedVehicleProblem timedVehicleProblem : vehicleProblems) {
                //...to find a vehicleProblem...
                if (timedVehicleProblem.getVehicleProblem() == VehicleProblem.Delay) {
                    // ...which is a Delay...
                    if (this.vehicleDelays.containsKey(vehicle.getId())) {
                        if (this.vehicleDelays.get(vehicle.getId()).getDelay() < LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM) {
                            // ...and does not have "enough" delay
                            toRemoveTimedVehicleProblem = timedVehicleProblem;
                            break; // there can only be one Delay Problem in each vehicle
                        }
                    } else {
                        this.logger.warn("VehicleDelays does not contain the following vehicle: \"" + vehicle.getId() + "\"");
                    }
                }
            }
            if (toRemoveTimedVehicleProblem != null) {
                vehicleProblems.remove(toRemoveTimedVehicleProblem);
            }
        }
    }

    /**
     * This method update the map vehicleDelays and calculates new delays depending on
     * LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN and LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN
     */
    private void updateDelays() {
        updateVehiclesDelayList(); //make sure vehicleDelays contains all currently driving vehicles (i.e. their IDs)
        for (Delay delay : vehicleDelays.values()) {
            int delayIncrease = 0;
            int delayDecrease = 0;

            /* There is a certain probability for a tour to increase and decrease its delay. We take the original delay,
               add the increase to it and subtract the decrease from it to have a new delay. */
            if (Math.random() * 100 < LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN) {
                delayIncrease = (int) (LIVE_ENGINE_FREQUENCY_SECONDS * Math.random());
            }
            if (delay.getDelay() != 0) {
                if (Math.random() * 100 < LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN) {
                    delayDecrease = (int) (LIVE_ENGINE_FREQUENCY_SECONDS * Math.random());
                }
            }

            int actualDelay = delay.getDelay() + delayIncrease - delayDecrease;
            if (actualDelay < 0) {
                actualDelay = 0;
            }
            delay.setDelay(actualDelay);
        }
    }

    /**
     * Helper method:
     * This method updates the list vehicleDelays so it contains all vehicleIds of vehicles which are currently running
     */
    private void updateVehiclesDelayList() {
        TourStore tourStore = ServiceRegistry.getService(TourStore.class);
        Network network = ServiceRegistry.getService(Network.class);
        List<Tour> currentRunningTours = new ArrayList<>();

        List<Line> lines = network.getLines();
        for (Line line : lines) {
            currentRunningTours.addAll(tourStore.getCurrentToursWithDelays(line, vehicleDelays));
        }
        List<String> currentlyRunningVehicleIds = currentRunningTours.stream().map(tours -> tours.getVehicle()).collect(Collectors.toList());

        // fill map if it is not filled yet
        if (vehicleDelays.size() == 0) {
            for (String vehicleId : currentlyRunningVehicleIds) {
                vehicleDelays.put(vehicleId, new Delay(null, null, 0)); // positions are calculated when need
            }
        } else { // update map if it is already filled
            List<String> currentDelayVehicleIds = new ArrayList<>();
            currentDelayVehicleIds.addAll(vehicleDelays.keySet());
            // copy list to find vehicles which are not running any more
            List<String> delayVehicleIdsNotRunningAnyMore = new ArrayList<>(currentDelayVehicleIds);

            for (String runningVehicleId : currentlyRunningVehicleIds) {
                if (currentDelayVehicleIds.contains(runningVehicleId)) {
                    delayVehicleIdsNotRunningAnyMore.remove(runningVehicleId);
                }
                else { // if the vehicle is not added yet add it
                    vehicleDelays.put(runningVehicleId, new Delay(null, null, 0));
                }
            }

            // delete all vehicles which are not running any more...
            for (String notRunnningVehicleId : delayVehicleIdsNotRunningAnyMore) {
                // ... and remove Delay problem
                FleetService fleet = ServiceRegistry.getService(FleetService.class);
                Vehicle vehicle = fleet.getVehicleForId(notRunnningVehicleId);
                TimedVehicleProblem vehicleProblemToBeDeleted = null;
                for (TimedVehicleProblem timedVehicleProblem : vehicle.getProblems()) {
                    if (timedVehicleProblem.getVehicleProblem() == VehicleProblem.Delay) {
                        vehicleProblemToBeDeleted = timedVehicleProblem;
                        break;
                    }
                }
                if (vehicleProblemToBeDeleted != null) {
                    vehicle.getProblems().remove(vehicleProblemToBeDeleted);
                }

                vehicleDelays.remove(notRunnningVehicleId);
            }
        }
    }

    // currently not used any more. Maybe use this later again if this distribution is better
    /*
    private int determineExponentiallyDistributedDelayInSeconds() { //usual delay returned by this method is something like 1-5 minutes
        Random random = new Random();
        int delayExponentiallyDistributed = (int) Math.round(1 + Math.log(1 - random.nextDouble()) / (-2.5) * 5);
        int delaySeconds = delayExponentiallyDistributed * 60;
        if (random.nextInt(2) == 1) { //add an extra 30 seconds with a probability of 50%
            delaySeconds += 30;
        }
        if (delaySeconds > 30 * 60) { //maximum delay of 30 minutes
            delaySeconds = 30 * 60;
        }
        return delaySeconds;
    } */

    @Override
    public List<Feedback> getNewFeedbacks() {
        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);

        Date now = new Date();
        List<Feedback> newFeedbacks = feedbackService.getFeedbacksInTimePeriod(
                new Timestamp(this.lastCallOfNewFeedbacksMethod), new Timestamp(now.getTime()));
        this.lastCallOfNewFeedbacksMethod = now.getTime();
        return newFeedbacks;
    }

    /**
     * Sets all the variables in the passed live engine according to "this" object
     * This is needed because we have to create a new live engine object when restarting the live engine after the frontend requested it
     * @param newLiveEngine the live engine for which we want to fill the configuration variables
     */
    private void transferVariablesFromFrontendConfiguredLiveEngineToNewLiveEngine(LiveEngine newLiveEngine) {
        newLiveEngine.setLIVE_ENGINE_FREQUENCY_SECONDS(this.LIVE_ENGINE_FREQUENCY_SECONDS);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN(this.LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE(this.LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE);
        newLiveEngine.setLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE(this.LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE);
        newLiveEngine.setLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE(this.LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE);
        newLiveEngine.setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(this.LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE);
        newLiveEngine.setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN(this.LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(this.LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN);
        newLiveEngine.setLIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM(this.LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM);
        newLiveEngine.setLIVE_ENGINE_RUN_LIVE(this.LIVE_ENGINE_RUN_LIVE);
        newLiveEngine.setLIVE_ENGINE_FREQUENCY_SECONDS(this.LIVE_ENGINE_FREQUENCY_SECONDS);
    }

    @Override
    public String getName() {
        return LiveEngine.class.toString();
    }

    public HashMap<String, List<TimedVehicleProblem>> getNewVehicleProblems() {
        return newVehicleProblems;
    }

    public void setNewVehicleProblems(HashMap<String, List<TimedVehicleProblem>> newVehicleProblems) {
        this.newVehicleProblems = newVehicleProblems;
    }

    public HashMap<String, List<TimedStopProblem>> getNewStopProblems() {
        return newStopProblems;
    }

    public void setNewStopProblems(HashMap<String, List<TimedStopProblem>> newStopProblems) {
        this.newStopProblems = newStopProblems;
    }

    public HashMap<String, List<TimedLineProblem>> getNewLineProblems() {
        return newLineProblems;
    }

    public void setNewLineProblems(HashMap<String, List<TimedLineProblem>> newLineProblems) {
        this.newLineProblems = newLineProblems;
    }

    public long getLastCallOfNewFeedbacksMethod() {
        return lastCallOfNewFeedbacksMethod;
    }

    public LiveEngineCreativity getLiveEngineCreativity() {
        return liveEngineCreativity;
    }

    public void setLiveEngineCreativity(LiveEngineCreativity liveEngineCreativity) {
        this.liveEngineCreativity = liveEngineCreativity;
    }

    @Override
    public int getLIVE_ENGINE_FREQUENCY_SECONDS() {
        return LIVE_ENGINE_FREQUENCY_SECONDS;
    }

    @Override
    public void setLIVE_ENGINE_FREQUENCY_SECONDS(int LIVE_ENGINE_FREQUENCY_SECONDS) {
        this.LIVE_ENGINE_FREQUENCY_SECONDS = LIVE_ENGINE_FREQUENCY_SECONDS;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN() {
        return LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN) {
        this.LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN = LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE() {
        return LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE;
    }

    @Override
    public void setLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE) {
        this.LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE = LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE;
    }

    @Override
    public int getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE() {
        return LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE;
    }

    @Override
    public void setLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE) {
        this.LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE = LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE;
    }

    @Override
    public int getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE() {
        return LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE;
    }

    @Override
    public void setLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE) {
        this.LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE = LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE;
    }

    @Override
    public int getLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE() {
        return LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE;
    }

    @Override
    public void setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(int LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE) {
        this.LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE = LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE;
    }

    @Override
    public int getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN() {
        return LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN(int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN) {
        this.LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN = LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN() {
        return LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN;
    }

    @Override
    public void setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN) {
        this.LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN = LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN;
    }

    @Override
    public int getLIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM() {
        return LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM;
    }

    @Override
    public void setLIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM(int LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM) {
        this.LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM = LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM;
    }

    @Override
    public boolean isLIVE_ENGINE_RUN_LIVE() {
        return LIVE_ENGINE_RUN_LIVE;
    }

    @Override
    public void setLIVE_ENGINE_RUN_LIVE(boolean LIVE_ENGINE_RUN_LIVE) {
        this.LIVE_ENGINE_RUN_LIVE = LIVE_ENGINE_RUN_LIVE;
    }
    
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}