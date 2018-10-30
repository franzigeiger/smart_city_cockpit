package de.se.data;

import de.se.data.enums.LineProblem;

import java.util.Date;

public class TimedLineProblem extends TimedProblem {

    public TimedLineProblem(LineProblem lineProblem) {
        this(lineProblem, new Date());
    }

    public TimedLineProblem(LineProblem lineProblem, Date date) {
        super(new GeneralProblem(lineProblem), date);
        int severity=0;
        switch(lineProblem) {
            case Overflow: severity = 2;
                break;
            case Obstacle: severity = 1;
                break;
            case General: severity = 1;
            break;
        }

        this.setSeverity(severity);

    }

    public LineProblem getLineProblem() {
        return LineProblem.valueOf(this.getGeneralProblem().getProblemDescription());
    }

    public void setLineProblem(LineProblem lineProblem) {
        this.setGeneralProblem(new GeneralProblem(lineProblem));
    }
}
