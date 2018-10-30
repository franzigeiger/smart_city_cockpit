package de.se.data;

import de.se.DB.hibernate_models.Vehicledb;
import de.se.data.enums.StateType;
import de.se.data.enums.VehicleProblem;
import de.se.model.interfaces.HasState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class contains all information about a vehicle.
 * It enriches VehicleDB objects with additional information to produce Vehicle objects.
 */
public class Vehicle implements RequestItem, HasState {

    private Vehicledb vehicleDB;

    private Position actualPosition;
    private Position plannedPosition;
    private List<TimedVehicleProblem> problems;

    private Tour actualTour;
    private int delayTimeInSeconds;

    public Vehicle(String id, String type, Position actualPosition, Position plannedPosition,
                   List<TimedVehicleProblem> problems, Tour actualTour, int delayTimeInSeconds) {
        this.vehicleDB = new Vehicledb();
        this.vehicleDB.setId(id);
        this.vehicleDB.setType(type);
        this.vehicleDB.setDeleted(false);

        this.actualPosition = actualPosition; //with delay
        this.plannedPosition = plannedPosition; //without delay
        this.problems = problems;

        if (problems == null) {
            problems = new ArrayList<TimedVehicleProblem>();
        }

        this.actualTour = actualTour;
        this.delayTimeInSeconds = delayTimeInSeconds;
    }

    public Vehicle(Vehicledb vehicleDB) {
        this.vehicleDB = vehicleDB;

        this.actualPosition = null;
        this.plannedPosition = null;
        this.problems = new ArrayList<TimedVehicleProblem>();
        this.actualTour = null;
        this.delayTimeInSeconds = 0;
    }

    public Vehicledb getVehicleDB() {
        return vehicleDB;
    }

    public String getId() {
        return vehicleDB.getId();
    }

    public String getType() {
        return vehicleDB.getType();
    }

    public Tour getActualTour() {
        return actualTour;
    }

    public Position getActualPosition() {
        return actualPosition;
    }

    public List<TimedVehicleProblem> getProblems() {
        return problems;
    }

    @Override
    public String getRequestItemID() {
        return this.vehicleDB.getId();
    }

    @Override
    public String getDescription() {
        return this.vehicleDB.getType();
    }

    public boolean isDeleted() {
        return this.vehicleDB.getDeleted();
    }

    public void setVehicleDB(Vehicledb vehicleDB) {
        this.vehicleDB = vehicleDB;
    }

    public void setActualPosition(Position actualPosition) {
        this.actualPosition = actualPosition;
    }

    public Position getPlannedPosition() {
        return plannedPosition;
    }

    public void setPlannedPosition(Position plannedPosition) {
        this.plannedPosition = plannedPosition;
    }

    /**
     * Set the list to the given list but does not allow duplicated problems
     *
     * @param problems which should be set as the new problem list
     */
    public void setProblems(List<TimedVehicleProblem> problems) {
        if (problems == null) {
            this.problems = new ArrayList<>();
            return;
        }
        List<TimedVehicleProblem> newProblemList = new ArrayList<>();
        for (TimedVehicleProblem timedVehicleProblem : problems) {
            if (!isListContainingProblemType(newProblemList, timedVehicleProblem.getVehicleProblem())) {
                newProblemList.add(timedVehicleProblem);
            }
        }
        this.problems = newProblemList;
    }

    /**
     * This method return whether a VehicleProblem-Typ is already in the given problems list
     *
     * @param problems   list containing problems
     * @param problemType for which we want to know if it is already in the list
     * @return whether the problem typ is already in the given problems list
     */
    private boolean isListContainingProblemType(List<TimedVehicleProblem> problems, VehicleProblem problemType) {
        for (TimedVehicleProblem timedVehicleProblem : problems) {
            if (timedVehicleProblem.getVehicleProblem() == problemType) {
                return true;
            }
        }
        return false;
    }

    public void setActualTour(Tour actualTour) {
        this.actualTour = actualTour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;

        Vehicle vehicle = (Vehicle) o;

        if (vehicleDB != null && !vehicleDB.getId().equals(vehicle.vehicleDB.getId())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = vehicleDB != null ? vehicleDB.hashCode() : 0;
        return result;
    }

    /**
     * Adds the given problem to the vehicle when there is not a problem with the same typ in the problem list
     *
     * @param problem which should be added
     */
    public void addProblem(TimedVehicleProblem problem) {
        if (!isListContainingProblemType(this.problems, problem.getVehicleProblem())) {
            problems.add(problem);
        }
    }

    public void setId(String id) {
        this.vehicleDB.setId(id);
    }

    public void setDelayTimeInSeconds(int delayTimeInSeconds) {
        this.delayTimeInSeconds = delayTimeInSeconds;
    }

    public int getDelayTimeInSeconds() {
        return delayTimeInSeconds;
    }

    /**
     * This method generates the state of the vehicle.
     *
     * @return
     */
    @Override
    public State getState() {
        int highSeverityCount = 0;
        for (TimedVehicleProblem problem : problems) {
            if (problem.getSeverity() != 1) {
                highSeverityCount++;
            }
        }

        StateType state = this.problems.size() >= 1 ? StateType.Yellow : StateType.Green;

        state = this.problems.size() > 3 || highSeverityCount > 0 ? StateType.Red : state;

        return new State(state, new ProblemElement("Vehicle", this.getId(), this.problems), null);
    }

    public void removeProblem(Date date) {
        for (TimedVehicleProblem problem : problems) {
            if (problem.getDate().equals(date)) {
                problems.remove(problem);
                return;
            }
        }
    }
}