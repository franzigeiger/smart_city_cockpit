package de.se.data;

import de.se.data.enums.StateType;

import java.util.List;

/**
 * This class contains a state element for all object which can have a state is this the return type.
 */
public class State {

    private StateType type;

   private ProblemElement problem;

    /**
     * This attribute contains the state of subelements of this element. If there are no subelements, there is no substate.
     */
   private List<State> substates;

    public State(StateType type, ProblemElement element, List<State> substates) {
        this.type = type;
        this.problem=element;
        this.substates = substates;
    }

    public StateType getType() {
        return type;
    }

    public ProblemElement getProblem() {
        return problem;
    }

    public List<State> getSubstates() {
        return substates;
    }
}
