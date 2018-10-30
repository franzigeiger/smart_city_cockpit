package de.se.model.interfaces;

import de.se.data.*;
import de.se.model.ParentService;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface FleetService extends ParentService, HasState{

    /**
     * Gets all vehicles which drive in this moment on the given line
     * @return a list of all vehicles for tubes
     */
    List<Vehicle> getVehiclesForLine(Line line);

    /**
     * Saves the given vehicle to the database
     * @param vehicle the vehicle to be saved in the database
     */
    public void saveVehicle(Vehicle vehicle) throws Exception;

    /**
     * This method returns all vehicle objects, including deleted vehicles.
     * @return
     */
    public List<Vehicle> getAllVehicles();

    /**
     * This method returns a list of all actual and planned tours for a vehicle
     * @param vehicleID
     * @return
     */
    public List<Tour> getShiftPlan(String vehicleID);

    /**
     * This method return a vehicle from the system.
     * @param vehicle
     */
    public void removeVehicle(String vehicle) throws Exception;

    /**
     * Updates this.vehicles with newly added problems and generated current positions
     * This method is called from the live engine. After that, the Network is responsible for
     * the consistency of the vehicles data (i.e. the problems and the positions)
     * @param problemsToAdd a hash map from the vehicle ID identifying a vehicle to a list of vehicle problems
     * @param positionsWithDelays a hash map from the vehicle ID to a Delay object,
     *                  containing the expected and actual position of the vehicle
     */
    void updateVehicles(HashMap<String, List<TimedVehicleProblem>> problemsToAdd, HashMap<String, Delay> positionsWithDelays);


    /**
     * Since the position generation process takes very long a little method to get all vehicles without actual position
     * is useful in some cases.
     * @return a list of vehicles, not actual position!
     */
    Collection<Vehicle> getNotLiveVehicles();

    /**
     * This method returns a full vehicle object for a given vehicle id.
     * Also if the vehicle was deleted it is possible to fetch the element by this method.
     * @param id
     * @return
     */
    public Vehicle getVehicleForId(String id);


    /**
     * This method return a string list
     * @param fullVehicle
     */
    List<String> getLinesForVehicle(Vehicle fullVehicle);

    /**
     * This method removes the problem with given generationTiem from the vehicle with the given id.
     * @param vehicleId
     * @param generationTime
     */
    public void removeVehicleProblem(String vehicleId, Date generationTime);

    /**
     * This method is for test issues to really remove vehicles again.
     * @param timeId
     */
    boolean deleteVehicleFromDb(String timeId);
}
