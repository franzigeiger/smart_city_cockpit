package de.se.data;

import de.se.data.enums.LineProblem;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleProblem;

public class GeneralProblem {
    private String problemType; //"Vehicle", "Stop" or "Line"
    private String problemDescription; //e.g. "Accident" or "Overcrowded"

    public GeneralProblem(VehicleProblem vehicleProblem) {
        this.problemType = "Vehicle";
        this.problemDescription = vehicleProblem.toString();
    }

    public GeneralProblem(StopProblem stopProblem) {
        this.problemType = "Stop";
        this.problemDescription = stopProblem.toString();
    }

    public GeneralProblem(LineProblem lineProblem) {
        this.problemType = "Line";
        this.problemDescription = lineProblem.toString();
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }
}
