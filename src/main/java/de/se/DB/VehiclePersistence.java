package de.se.DB;

import de.se.data.Vehicle;

import java.util.List;

/**
 * Fetches and saves vehicles to database
 */
public interface VehiclePersistence {
    /**
     * Fetches vehicles from the database
     * @return a list of all Vehicle objects from the database
     */
    List<Vehicle> fetchVehicles();

    /**
     * Saves a Vehicle object to the database.
     * A mapping from Vehicle to VehicleDB has to be performed in this method
     * because the database only works with VehicleDB objects and not Vehicle objects.
     * @param vehicle the Vehicle object to be saved in the database
     */
    void saveVehicle(Vehicle vehicle);

    /**
     * Delete the give Vehicle from the database.
     * @param vehicle Object which contains the database reference which should be deleted
     */
    void deleteVehicle(Vehicle vehicle);

    /**
     * Marks the vehilce as delete without actually deleting it from the database
     * @param vehicle Object which contains the database reference which should be marked as deleted
     */
    void setVehicleAsDeleted(Vehicle vehicle);

    /**
     * @return a list of Vehicles which are marked as deleted
     */
    //List<Vehicle> getVehiclesMarkedAsDeleted();
}
