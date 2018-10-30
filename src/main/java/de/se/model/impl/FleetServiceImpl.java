package de.se.model.impl;

import de.se.DB.VehiclePersistence;
import de.se.DB.hibernate_models.Vehicledb;
import de.se.DB.impl.VehiclePersistenceImpl;
import de.se.data.*;
import de.se.data.enums.StateType;
import de.se.data.enums.VehicleEnum;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.LiveEngine;
import de.se.model.interfaces.TourStore;
import de.se.model.mocks.FleetMock;

import java.sql.Timestamp;
import java.util.*;

public class FleetServiceImpl implements FleetService {

    VehiclePersistence persistence;
    Map<String, Vehicle> vehicles;
    Map<String, Vehicle> deletedVehicles;

    public FleetServiceImpl() {
        persistence = new VehiclePersistenceImpl();
    }

    @Override
    public void initialize() {
        initDB();
        List<Vehicle> vehicleList = persistence.fetchVehicles();
        vehicles = new HashMap<String, Vehicle>();
        deletedVehicles = new HashMap<String, Vehicle>();
        for (Vehicle v : vehicleList) {
            if (v.isDeleted()) {
                deletedVehicles.put(v.getId(), v);
            } else {
                vehicles.put(v.getId(), v);
            }
        }
        //vehicles.put("V1", new Vehicle("V1" , VehicleEnum.Bus.toString() , new Position(null, null , true) , new ArrayList<VehicleProblem>() ));
    }

    private void initDB() {
        if (persistence.fetchVehicles().size() == 0) {
            saveAllFabricatedVehiclesForTubes();
            saveAllFabricatedVehiclesForBuses();
        }
    }

    @Override
    public List<Vehicle> getVehiclesForLine(Line line) {
        TourStore store = ServiceRegistry.getService(TourStore.class);
        List<Tour> tours = store.getCurrentTours(line);

        List<Vehicle> lineVehicles = new ArrayList<Vehicle>();
        for (Tour tour : tours) {
            Vehicle vehicle = vehicles.get(tour.getVehicle());
            if(vehicle!= null){
                lineVehicles.add(vehicle);
            }

        }

        LiveEngine engine = ServiceRegistry.getService(LiveEngine.class);
        engine.enrichVehicles(lineVehicles);

        return lineVehicles;
    }

    @Override
    public void saveVehicle(Vehicle vehicle) throws Exception {
        if (vehicles.get(vehicle.getId()) != null) {
            throw new Exception("Vehicle name is already in system, please select another one!");
        }
        persistence.saveVehicle(vehicle);
        vehicles.put(vehicle.getId(), vehicle);
    }


    @Override
    public List<Vehicle> getAllVehicles() {
        LiveEngine engine = ServiceRegistry.getService(LiveEngine.class);
        if (vehicles == null) { //to avoid NullPointerException
            return new ArrayList<>();
        }
        engine.enrichVehicles(vehicles.values());
        List<Vehicle> vehicleWithDeleted = new ArrayList<>(this.vehicles.values());
        vehicleWithDeleted.addAll(deletedVehicles.values());

        return vehicleWithDeleted;
    }

    @Override
    public List<Tour> getShiftPlan(String vehicle) {
        TourStore store = ServiceRegistry.getService(TourStore.class);
        return store.getToursForVehicle(vehicles.get(vehicle).getId());
    }


    /**
     * If a vehicle gets deletes, which has still feedbacks or services a exception will appear if the feedback want this reference.
     * Because data is still available in db, but not in system.
     * We have to make a certain get-also-deleted-vehicles method for that special cases.
     *
     * @param vehicle
     * @throws Exception
     */
    @Override
    public void removeVehicle(String vehicle) throws Exception {
        //vehicle will not delete from DB. Only marked as deleted
        List<Tour> tours = this.getShiftPlan(vehicle);
        TourStore tourStore = ServiceRegistry.getService(TourStore.class);
        for (Tour tour : tours) {
            tourStore.updateTour(tour, "");
        }
        Vehicle vehicleToDelete = vehicles.get(vehicle);
        persistence.setVehicleAsDeleted(vehicleToDelete);
        vehicles.remove(vehicle);
        deletedVehicles.put(vehicle, vehicleToDelete);
    }

    @Override
    public String getName() {
        return "FleetService";
    }

    @Override
    public State getState() {
        List<State> problems = new ArrayList<State>();
        int errorStates = 0;
        int warningStates = 0;

        for (Vehicle v : vehicles.values()) {
            State state = v.getState();
            problems.add(state);
            if (state.getType().equals(StateType.Red)) {
                errorStates++;
            }
            if (state.getType().equals(StateType.Yellow)) {
                warningStates++;
            }
        }

        StateType state = warningStates > 0 ? StateType.Yellow : StateType.Green;

        state = (errorStates > 0 || warningStates > 4) ? StateType.Red : state;

        return new State(state, null, problems);
    }

    /**
     * Save 100 Vehicles of typ tube are added to the Database
     */
    void saveAllFabricatedVehiclesForTubes() {
        for (int i = 0; i < 100; ++i) {
            addVehicleToDB(i, VehicleEnum.Tube.toString());
        }
    }

    /**
     * 100 Vehicles of type bus are saved in the DB
     */
    void saveAllFabricatedVehiclesForBuses() {
        for (int i = 0; i < 60; ++i) {
            addVehicleToDB(i, VehicleEnum.Bus.toString());
        }
    }

    private void addVehicleToDB(int index, String type) {
        Vehicledb vehicledb = new Vehicledb();
        vehicledb.setType(type);
        if (type.equals("Tube")) {
            vehicledb.setId("T_" + String.format("%03d", index));
        } else if (type.equals("Bus")) {
            vehicledb.setId("B_" + String.format("%03d", index));
        } else {
            vehicledb.setId("Vehicle_" + String.format("%03d", index));
        }
        vehicledb.setDeleted(false);
        Vehicle vehicle = new Vehicle(vehicledb);
        persistence.saveVehicle(vehicle);
    }


    @Override
    public void updateVehicles(HashMap<String, List<TimedVehicleProblem>> problemsToAdd, HashMap<String, Delay> vehiclePositionsWithDelays) {
        for (Vehicle vehicle : this.vehicles.values()) {
            if (problemsToAdd.containsKey(vehicle.getId())) {
                for (TimedVehicleProblem problem : problemsToAdd.get(vehicle.getId())) {
                    vehicle.addProblem(problem);
                }
            }

            if (vehiclePositionsWithDelays.containsKey(vehicle.getId())) {
                vehicle.setActualPosition(vehiclePositionsWithDelays.get(vehicle.getId()).getActualPosition());
                vehicle.setPlannedPosition(vehiclePositionsWithDelays.get(vehicle.getId()).getExpectedPosition());
                vehicle.setDelayTimeInSeconds(vehiclePositionsWithDelays.get(vehicle.getId()).getDelay());
            }
        }
    }

    @Override
    public void removeVehicleProblem(String vehicleId, Date generationTime) {
        vehicles.get(vehicleId).removeProblem(generationTime);
    }

    @Override
    public Collection<Vehicle> getNotLiveVehicles() {
        return vehicles.values();
    }

    @Override
    public Vehicle getVehicleForId(String id) {
        return vehicles.get(id) != null ? vehicles.get(id) : deletedVehicles.get(id);
    }

    @Override
    public List<String> getLinesForVehicle(Vehicle fullVehicle) {
        TourStore store = ServiceRegistry.getService(TourStore.class);
        List<Tour> tours = store.getToursForVehicle(fullVehicle.getId());

        List<String> lines = new ArrayList<>();
        for (Tour tour : tours) {
            if (!lines.contains(tour.getLine())) {
                lines.add(tour.getLine());
            }
        }

        return lines;
    }

    public void setPersister(VehiclePersistence persister) {
        this.persistence = persister;
    }

    @Override
    public boolean deleteVehicleFromDb(String vehicleId) {
        if (deletedVehicles.get(vehicleId) != null) {
            persistence.deleteVehicle(deletedVehicles.get(vehicleId));
            return true;
        }

        return false;

    }
}
