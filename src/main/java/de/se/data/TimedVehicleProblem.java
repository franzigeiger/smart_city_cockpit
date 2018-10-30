package de.se.data;

import de.se.data.enums.VehicleProblem;

import java.util.Date;

public class TimedVehicleProblem extends TimedProblem {
    public TimedVehicleProblem(VehicleProblem vehicleProblem) {
        this(vehicleProblem , new Date());
    }

    public TimedVehicleProblem(VehicleProblem vehicleProblem, Date date) {
        super(new GeneralProblem(vehicleProblem), date);
        int severity=0;
        switch(vehicleProblem) {
            case Accident: severity = 2;
                break;
            case Delay: severity = 1;
                break;
            case Defect_Engine: severity = 2;
                break;
            case Dirty: severity = 1;
                break;
            case Defect_Air_Conditioner: severity = 1;
                break;
            case Defect_Door: severity = 1;
                break;
            case Defect_Wheel: severity = 2;
                break;
            case General: severity = 1;
                break;
            default: severity = 1;
        }

        this.setSeverity(severity);
    }

    public VehicleProblem getVehicleProblem() {
        return VehicleProblem.valueOf(this.getGeneralProblem().getProblemDescription());
    }

    public void setVehicleProblem(VehicleProblem vehicleProblem) {
        this.setGeneralProblem(new GeneralProblem(vehicleProblem));
    }
}
