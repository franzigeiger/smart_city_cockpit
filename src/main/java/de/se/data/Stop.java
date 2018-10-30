package de.se.data;

import de.se.DB.hibernate_models.Stopdb;
import de.se.data.enums.StateType;
import de.se.data.enums.StopProblem;
import de.se.model.interfaces.HasState;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all information about a stop.
 * It enriches StopDB objects with additional information to produce Stop objects.
 */
public class Stop implements HasState, RequestItem {

    private Stopdb stopDB;

    private List<TimedStopProblem> problems;
    private boolean closed; // if this is set true vehicles will skips this stop

    public Stop(String id, String name, String latitude, String longitude, List<TimedStopProblem> problems) {
        this.stopDB= new Stopdb();
        this.stopDB.setId(id);
        this.stopDB.setName(name);
        this.stopDB.setLatitude(latitude);
        this.stopDB.setLongitude(longitude);

        setProblems(problems); // for avoiding duplicates
        this.closed = false;

        if (problems == null){
            this.problems = new ArrayList<TimedStopProblem>();
        }
    }

      public Stop(Stopdb stopDB) {
            this.stopDB = stopDB;

            this.problems = new ArrayList<TimedStopProblem>();
          this.closed = false;
        }

        public Stop(String id) {
            Stopdb stopdb = new Stopdb();
            stopdb.setId(id);

            stopdb.setName(null);
            stopdb.setLatitude(null);
            stopdb.setLongitude(null);

            this.stopDB = stopdb;
            this.problems = new ArrayList<TimedStopProblem>();
            this.closed = false;
        }

    @Override
    public State getState() {
        int highSeverityCount =0;
        int middleSeverityCount =0;
        for(TimedStopProblem problem : problems ){
            if (problem.getSeverity() == 1) {
                middleSeverityCount++;
            } else {
                highSeverityCount++;
            }
        }

        StateType state = problems.size() > 0 ? StateType.Yellow : StateType.Green;

        state = problems.size() >= 3 || highSeverityCount > 0  ? StateType.Red : state;

        return new State(state, new ProblemElement("Stop" , this.getId() , this.problems), null);
    }

    public Stopdb getStopDB() {
        return stopDB;
    }

    public String getId() {
        return stopDB.getId();
    }

    public String getName() {
        return stopDB.getName();
    }

    public String getLatitude() {
        return this.stopDB.getLatitude();
    }

    public String getLongitude() {
        return this.stopDB.getLongitude();
    }

    public void setName(String name) {
        this.stopDB.setName(name);
    }

    public void setStopDB(Stopdb stopDB) {
        this.stopDB = stopDB;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Set the list to the given list but does not allow duplicated problems
     *
     * @param problems which should be set as the new problem list
     */
    public void setProblems(List<TimedStopProblem> problems) {
        if (problems == null) {
            this.problems = new ArrayList<>();
            return;
        }
        List<TimedStopProblem> newProblemList = new ArrayList<>();
        for (TimedStopProblem timedStopProblem : problems) {
            if (!isListContainingProblemType(newProblemList, timedStopProblem.getStopProblem())) {
                newProblemList.add(timedStopProblem);
            }
        }
        this.problems = newProblemList;
    }

    @Override
    public String getRequestItemID() {
        return this.stopDB.getId();
    }

    public List<TimedStopProblem> getProblems() {
        return problems;
    }

    @Override
    public String getDescription() {
        return this.stopDB.getName();
    }

    public void setDescription(String description) {
        this.stopDB.setName(description);
    }

    /**
     * Adds the given problem to the vehicle when there is not a problem with the same typ in the problem list
     *
     * @param problem which should be added
     */
    public void addProblem(TimedStopProblem problem){
        if (!isListContainingProblemType(this.problems, problem.getStopProblem())) {
            problems.add(problem);
        }
    }

    /**
     * This method adds all problems to the problems list using the intern add function to avoid duplicates
     *
     * @param problems
     */
    public void addMultipleProblems(List<TimedStopProblem> problems) {
        for (TimedStopProblem timedStopProblem : problems) {
            addProblem(timedStopProblem);
        }
    }

    /**
     * This method return whether a VehicleProblem-Typ is already in the given problems list
     *
     * @param problems   list containing problems
     * @param problemType for which we want to know if it is already in the list
     * @return whether the problem typ is already in the given problems list
     */
    private boolean isListContainingProblemType(List<TimedStopProblem> problems, StopProblem problemType) {
        for (TimedStopProblem timedStopProblem : problems) {
            if (timedStopProblem.getStopProblem() == problemType) {
                return true;
            }
        }
        return false;
    }
}
