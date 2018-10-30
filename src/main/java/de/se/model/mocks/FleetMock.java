package de.se.model.mocks;

import de.se.data.*;
import de.se.data.enums.StateType;
import de.se.data.enums.VehicleEnum;
import de.se.data.enums.VehicleProblem;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.TourStore;

import java.util.*;

public class FleetMock implements FleetService{

    List<Vehicle> vehicles;
    @Override
    public List<Vehicle> getVehiclesForLine(Line line) {
        return vehicles;
    }

    @Override
    public void saveVehicle(Vehicle vehicle) {

    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicles;
    }

    @Override
    public List<Tour> getShiftPlan(String vehicleID) {
        return ServiceRegistry.getService(TourStore.class).getToursForLine(null);
    }

    @Override
    public void removeVehicle(String vehicle) {

    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void initialize() {
        vehicles = new ArrayList<Vehicle>();

        List<TimedVehicleProblem> problems = Arrays.asList(
                new TimedVehicleProblem(VehicleProblem.Accident),
                new TimedVehicleProblem(VehicleProblem.Delay)
        );

        List<TimedVehicleProblem> newList = new ArrayList<TimedVehicleProblem>(problems);
        vehicles.add(new Vehicle("V1", VehicleEnum.Bus.toString() , new Position("one", "two", true),
                new Position("one", "two", true), newList,
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V2", VehicleEnum.Bus.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), newList,
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V3", VehicleEnum.Bus.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), newList,
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V4", VehicleEnum.Tube.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), newList,
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));
        vehicles.add(new Vehicle("V5", VehicleEnum.Tube.toString() , new Position("three", "two", true) ,
                new Position("three", "two", true), newList,
                new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0));


        for (int i = 0; i < 100; i++) {
            Vehicle vehicle = new Vehicle("ID " + i, VehicleEnum.Tube.toString() , new Position("one", "two", true),
                    new Position("one", "two", true), newList,
                    new Tour(1, null, null,  "one", "two" , "V1" , "l1"), 0);
            List<TimedVehicleProblem> vehicleProblems = new ArrayList<>();
            vehicleProblems.add(new TimedVehicleProblem(VehicleProblem.Accident)); //because they are deletable for live engine
            vehicle.setProblems(vehicleProblems);
            this.vehicles.add(vehicle);
        }
    }

    @Override
    public State getState() {
       List<TimedVehicleProblem> list = new ArrayList<TimedVehicleProblem>();
        list.add(new TimedVehicleProblem(VehicleProblem.Delay));
        return new State(StateType.Red , null, Arrays.asList(new State(StateType.Red, new ProblemElement("Vehicle", "V1", list), null)));
    }

    @Override
    public void updateVehicles(HashMap<String, List<TimedVehicleProblem>> problemsToAdd, HashMap<String, Delay> positionsWithDelays) {

    }

    @Override
    public Collection<Vehicle> getNotLiveVehicles() {
        return vehicles;
    }

    @Override
    public Vehicle getVehicleForId(String id) {
        return vehicles.get(0);
    }

    @Override
    public List<String> getLinesForVehicle(Vehicle fullVehicle) {
        return Arrays.asList(
            "l1", "l2"
        );
    }

    @Override
    public void removeVehicleProblem(String vehicleId, Date generationTime) {

    }

    @Override
    public boolean deleteVehicleFromDb(String timeId) {
        return true;
    }
}