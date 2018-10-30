package de.se.data;

import java.util.List;

public class ProblemElement {
    private String problemType; //"Vehicle", "Stop" or "Line"
    String elemID;
    List<? extends TimedProblem> problem ;

    public ProblemElement(String problemType, String elemID, List<? extends TimedProblem> problem) {
        this.problemType = problemType;
        this.elemID = elemID;
        this.problem = problem;
    }

    public String getProblemType() {
        return problemType;
    }

    public String getElemID() {
        return elemID;
    }

    public List<? extends TimedProblem> getProblem() {
        return problem;
    }
}
