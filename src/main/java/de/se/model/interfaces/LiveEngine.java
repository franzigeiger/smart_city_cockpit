package de.se.model.interfaces;

import de.se.data.*;
import de.se.model.ParentService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public interface LiveEngine extends ParentService {

    /**
     * This method enriches vehicle objects by their current positions and a list of problems, i.e. by live data
     * @param vehicles the vehicles to be enriched
     */
    void enrichVehicles(Collection<Vehicle> vehicles);

    /**
     * This method enriches stop objects by a list of problems, i.e. by live data
     * @param stops the stops to be enriched
     */
    void enrichStops(Collection<Stop> stops);

    /**
     * This method enriches line objects by a list of problems, i.e. by live data
     * @param lines the lines to be enriched
     */
    void enrichLines(List<Line> lines);

    /**
     * This method return all new arrived feedbacks since the last call of this method
     * @return a list of the newly arrived feedbacks
     */
    List<Feedback> getNewFeedbacks();

    /**
     * This method should be called after all services have been initialized.
     * It starts up the live engine by generating initial problems according to the settings
     * and further starts the live engine thread that always runs and generates new problems
     * in an interval that has been configured.
     */
    void start();

    /**
     * Restarts the live engine by cancelling the existing timer task and starting a new one.
     * This is called in case the frontend has configured the live engine to restart
     */
    void restart();

    /**
     * Helper method for LiveEngineImplTest for not trying to start the live Engine twice
     * @return whether the LiveEngine is running already or not
     */
    boolean isLiveEngineStarted();

    /**
     * Generates amount many random vehicle problems
     * @param amount the amount of vehicle problems to generate
     */
    void generateVehicleProblems(int amount);

    /**
     * Generates amount many random stop problems
     * @param amount the amount of stop problems to generate
     */
    void generateStopProblems(int amount);

    /**
     * Generates amount many random line problems
     * @param amount the amount of line problems to generate
     */
    void generateLineProblems(int amount);

    /**
     * Deletes "amount" many vehicle problems
     * @param amount the amount of vehicle problems to delete
     * @return the amount of vehicle problems that were actually deleted
     */
    int deleteVehicleProblems(int amount);

    /**
     * Deletes "amount" many stop problems
     * @param amount the amount of stop problems to delete
     * @return the amount of stop problems that were actually deleted
     */
    int deleteStopProblems(int amount);

    /**
     * Deletes "amount" many line problems
     * @param amount the amount of line problems to delete
     * @return the amount of line problems that were actually deleted
     */
    int deleteLineProblems(int amount);

    /**
     * Generates the current position of the passed vehicles according to the timetable
     * @param vehicles the vehicles to generate the position for
     * @return a hash map from vehicle ID to position
     */
    HashMap<String, Position> generatePositions(Collection<Vehicle> vehicles);

    /**
     * Generates random delays for vehicles.
     * It calculates the expected and actual position along with the delay in seconds for each vehicle.
     * @param vehicles the vehicles for which random delays are to be generated
     * @return a hash map from vehicle ID to a Delay object, containing the expected stop,
     *      the actual stop (considering the delay) as well as the delay time in seconds
     */
    HashMap<String, Delay> generateDelays(Collection<Vehicle> vehicles);


    /**
     * Getters and setters for live engine configuration
     * These values can be set in the frontend
     */

    int getLIVE_ENGINE_FREQUENCY_SECONDS();
    void setLIVE_ENGINE_FREQUENCY_SECONDS(int LIVE_ENGINE_FREQUENCY_SECONDS);
    int getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN);
    int getLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN();
    void setLIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN(int LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN);
    int getLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE();
    void setLIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE);
    int getLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE();
    void setLIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE);
    int getLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE();
    void setLIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE(int LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE);
    int getLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE();
    void setLIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE(int LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE);
    int getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN();
    void setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN(int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN);
    int getLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN();
    void setLIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN(int LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN);
    int getLIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM();
    void setLIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM(int LIVE_ENGINE_MINIMUM_DELAY_SECONDS_FOR_CREATING_DELAY_PROBLEM);
    boolean isLIVE_ENGINE_RUN_LIVE();
    void setLIVE_ENGINE_RUN_LIVE(boolean LIVE_ENGINE_RUN_LIVE);

    void setTimer(Timer timer);
}
