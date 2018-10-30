package de.se.data;

import de.se.data.enums.StopProblem;
import org.apache.log4j.Logger;

import java.util.Date;

public class TimedStopProblem extends TimedProblem {

    private final Logger logger = Logger.getLogger(TimedStopProblem.class.toString());

    public TimedStopProblem(StopProblem stopProblem) {
        this(stopProblem , new Date());
    }

    public TimedStopProblem(StopProblem stopProblem, Date date) {
        super(new GeneralProblem(stopProblem), date);
        int severity=0;
        switch(stopProblem) {
            case Overflow: severity = 2;
                break;
            case Dirty: severity = 1;
                break;
            case Broken: severity = 1;
                break;
            default:
                this.logger.warn("Could not determine severity for stop problem \"" + stopProblem + "\"." +
                        "Setting default value of 1.");
                severity = 1;
                break;
        }

        this.setSeverity(severity);
    }

    public StopProblem getStopProblem() {
        return StopProblem.valueOf(this.getGeneralProblem().getProblemDescription());
    }

    public void setStopProblem(StopProblem stopProblem) {
        this.setGeneralProblem(new GeneralProblem(stopProblem));
    }
}
