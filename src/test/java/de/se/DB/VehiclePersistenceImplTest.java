package de.se.DB;

import de.se.DB.hibernate_models.Vehicledb;
import de.se.DB.impl.VehiclePersistenceImpl;
import de.se.data.Vehicle;
import de.se.data.enums.VehicleEnum;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VehiclePersistenceImplTest {
    private VehiclePersistence vehiclePersistence = new VehiclePersistenceImpl();
    private Vehicle lastInsertedVehilce = null;

    /**
     * Delete Test element when still exists
     */
    @After
    public void cleanDB() {
        if (lastInsertedVehilce != null && vehiclePersistence.fetchVehicles().contains(lastInsertedVehilce))
            vehiclePersistence.deleteVehicle(lastInsertedVehilce);
    }

    @Test
    public void fetchVehicles() throws Exception {
        List<Vehicle> vehicles = vehiclePersistence.fetchVehicles();
        assertNotNull(vehicles);
        assertTrue(vehicles.size() >= 160); // minimum should there be 100 tubes and 60 buses
    }

    @Test
    public void saveAndDeleteVehicle() throws Exception {
        List<Vehicle> oldVehicles = vehiclePersistence.fetchVehicles();
        Vehicledb vehicledb = new Vehicledb();
        vehicledb.setDeleted(false);
        vehicledb.setType(VehicleEnum.Tube.toString());
        vehicledb.setId("testVehicleForPersistence");
        Vehicle newVehicle = new Vehicle(vehicledb);
        vehiclePersistence.saveVehicle(newVehicle);
        lastInsertedVehilce = newVehicle;
        assertTrue(vehiclePersistence.fetchVehicles().contains(newVehicle));

        vehiclePersistence.deleteVehicle(newVehicle);
        assertEquals(oldVehicles.size(), vehiclePersistence.fetchVehicles().size());
    }
}
