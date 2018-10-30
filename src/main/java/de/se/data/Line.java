package de.se.data;

import de.se.DB.hibernate_models.Linedb;
import de.se.data.enums.LineProblem;
import de.se.data.enums.StateType;
import de.se.model.interfaces.HasState;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all information about a line.
 * It enriches LineDB objects with additional information to produce Line objects.
 */
public class Line implements HasState {

    private Linedb lineDB;

    private Route route;
    private List<TimedLineProblem> problems;


    public Line(String id, String name, String vehicleType, Route route, String color) {
        this.lineDB = new Linedb();
        this.lineDB.setId(id);
        this.lineDB.setName(name);
        this.lineDB.setType(vehicleType);
        this.lineDB.setColor(color);

        this.route = route;
        this.problems = new ArrayList<TimedLineProblem>();
    }

    public Line(Linedb lineDB) {
        this.lineDB = lineDB;

        this.route = null;
        this.problems = new ArrayList<TimedLineProblem>();
    }

    @Override
    public State getState() {
        List<State> substates = new ArrayList<State>();

        for(Stop stop : route.getStopSequence()){
            substates.add(stop.getState());
        }

        int errorSubStates =0;
        int warningSubstates =0;
        for(State state : substates){
            if(state.getType().equals(StateType.Yellow)){
                warningSubstates++;
            }

            if(state.getType().equals(StateType.Red)){
                errorSubStates++;
            }
        }

        StateType state = problems.size() >= 1 || warningSubstates > 0 ? StateType.Yellow : StateType.Green;

        state = problems.size() > 2 || errorSubStates > 0 || warningSubstates >= 3 ? StateType.Red : state;

        return new State(state, new ProblemElement("Line" , this.getId(), problems), substates);
    }

    public Linedb getLineDB() {
        return lineDB;
    }

    public String getId() {
        return lineDB.getId();
    }

    public String getName() {
        return lineDB.getName();
    }

    public String getVehicleType() {
        return lineDB.getType();
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getColor(){
        return lineDB.getColor();
    }

    /**
     * Adds the given problem to the vehicle when there is not a problem with the same typ in the problem list
     *
     * @param problem which should be added
     */
    public void addProblem(TimedLineProblem problem){
        if (!isListContainingProblemType(this.problems, problem.getLineProblem())) {
            problems.add(problem);
        }
    }

    /**
     * This method adds all problems to the problems list using the intern add function to avoid duplicates
     *
     * @param problems
     */
    public void addMultipleProblems(List<TimedLineProblem> problems) {
        for (TimedLineProblem timedLineProblem : problems) {
            addProblem(timedLineProblem);
        }
    }

    public void setLineDB(Linedb lineDB) {
        this.lineDB = lineDB;
    }

    public List<TimedLineProblem> getProblems() {
        return problems;
    }

    /**
     * Set the list to the given list but does not allow duplicated problems
     *
     * @param problems which should be set as the new problem list
     */
    public void setProblems(List<TimedLineProblem> problems) {
        if (problems == null) {
            this.problems = new ArrayList<>();
            return;
        }
        List<TimedLineProblem> newProblemList = new ArrayList<>();
        for (TimedLineProblem timedLineProblem : problems) {
            if (!isListContainingProblemType(newProblemList, timedLineProblem.getLineProblem())) {
                newProblemList.add(timedLineProblem);
            }
        }
        this.problems = newProblemList;
    }

    /**
     * This method return whether a LineProblem-Typ is already in the given problems list
     *
     * @param problems   list containing problems
     * @param problemType for which we want to know if it is already in the list
     * @return whether the problem typ is already in the given problems list
     */
    private boolean isListContainingProblemType(List<TimedLineProblem> problems, LineProblem problemType) {
        for (TimedLineProblem timedLineProblem : problems) {
            if (timedLineProblem.getLineProblem() == problemType) {
                return true;
            }
        }
        return false;
    }
}
