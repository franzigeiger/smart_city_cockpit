package de.se.model.mocks;

import de.se.DB.VehiclePersistence;
import de.se.data.Position;
import de.se.data.TimedVehicleProblem;
import de.se.data.Tour;
import de.se.data.Vehicle;
import de.se.data.enums.VehicleEnum;
import de.se.data.enums.VehicleProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VehiclePersistenceMock implements VehiclePersistence {
    @Override
    public List<Vehicle> fetchVehicles() {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        List<TimedVehicleProblem> problems = Arrays.asList(
                new TimedVehicleProblem(VehicleProblem.Delay)
        );

        vehicles.add(new Vehicle("V1", VehicleEnum.Tube.toString() , new Position("one", "two", true),
                new Position("one", "two", true), new ArrayList<TimedVehicleProblem>(problems),
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V2", VehicleEnum.Tube.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), new ArrayList<TimedVehicleProblem>(problems),
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V3", VehicleEnum.Bus.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), new ArrayList<TimedVehicleProblem>(problems),
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V4", VehicleEnum.Tube.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), new ArrayList<TimedVehicleProblem>(),
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V5", VehicleEnum.Tube.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), new ArrayList<TimedVehicleProblem>(),
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));

        vehicles.get(4).getVehicleDB().setDeleted(true);

        return vehicles;
    }

    @Override
    public void saveVehicle(Vehicle vehicle) {

    }

    @Override
    public void deleteVehicle(Vehicle vehicle) {

    }

    @Override
    public void setVehicleAsDeleted(Vehicle vehicle) {
          vehicle.getVehicleDB().setDeleted(true);
    }
}
