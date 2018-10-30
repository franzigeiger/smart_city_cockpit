package de.se.DB.impl;

import de.se.DB.VehiclePersistence;
import de.se.DB.hibernate_models.Vehicledb;
import de.se.DB.GeneralPersistence;
import de.se.data.Vehicle;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class VehiclePersistenceImpl extends GeneralPersistence implements VehiclePersistence {
    @Override
    public List<Vehicle> fetchVehicles() {
        return getVehilcesFromQuery("FROM Vehicledb");
    }

    @Override
    public void saveVehicle(Vehicle vehicle) {
        saveObjectToDatabase(vehicle.getVehicleDB());
    }

    @Override
    public void setVehicleAsDeleted(Vehicle vehicle) {
        vehicle.getVehicleDB().setDeleted(true);
    }

    @Override
    public void deleteVehicle(Vehicle vehicle) {
        deleteObjectFromDatabase(vehicle.getVehicleDB());
    }

    // Martin: works but is not useable at the moment since the deleted attribute is not pused to the DB
    /*@Override
    public List<Vehicle> getVehiclesMarkedAsDeleted() {
        return getVehilcesFromQuery("FROM Vehicledb WHERE deleted = 'true'");
    }*/

    private List<Vehicle> getVehilcesFromQuery(String query) {
        final Session session = getNewSession();
        List<Vehicledb> vehiclesDBs = session.createQuery(query).list();

        List<Vehicle> vehicles = new ArrayList<>();
        for (Vehicledb vehicleDB : vehiclesDBs) {
            vehicles.add(new Vehicle(vehicleDB));
        }

        session.close();
        return vehicles;
    }
}
