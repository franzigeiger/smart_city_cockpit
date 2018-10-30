package de.se.data;

import java.util.Date;

/**
 * This class brings together the different enums {Vehicle, Stop, Line}Problem and enriches it
 * with a date and an author to produce a TimedProblem.
 */
public class TimedProblem {
    private GeneralProblem generalProblem;
    private Date date;
    private int severity;

    public TimedProblem(GeneralProblem generalProblem, Date date) {
        this.generalProblem = generalProblem;
        this.date = date;
    }

    public GeneralProblem getGeneralProblem() {
        return generalProblem;
    }

    public void setGeneralProblem(GeneralProblem generalProblem) {
        this.generalProblem = generalProblem;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }
}
