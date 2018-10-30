package de.se.data.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * This enum contains all possible vehicle problems.
 */
public enum VehicleProblem {
    // do not change this order since the getRandomVehicleProblem() method excludes "Delay" by index
    Defect_Engine, Dirty, Accident, Overcrowded, Defect_Air_Conditioner, Defect_Door, General, Defect_Wheel, Delay;

    /**
     * Picks a random value of the VehicleProblem enum
     *
     * @return a random VehicleProblem
     */
    public static VehicleProblem getRandomVehicleProblem() {
        Random random = new Random();
        return values()[random.nextInt(values().length - 1)]; // do not return Delay Problem
    }

    /**
     * Does not return Defect_Wheel(no Passenger can give a Feedback on this) and
     * Delay (not allowed to be created randomly)
     *
     * @param enumNumber number of the Enum typ
     * @return the VehicleProblem related to the number
     */
    public static VehicleProblem getVehicleProblemByNumber(int enumNumber) {
        switch (enumNumber) {
            case 0:
                return VehicleProblem.Defect_Engine;
            case 1:
                return VehicleProblem.Dirty;
            case 2:
                return VehicleProblem.Accident;
            case 3:
                return VehicleProblem.Overcrowded;
            case 4:
                return VehicleProblem.Defect_Air_Conditioner;
            case 5:
                return VehicleProblem.Defect_Door;
            default:
                return VehicleProblem.General;
        }
    }

    /**
     * Not every type of problem may be deleted by the live engine because not every problem can be solved externally.
     * This method determines the types of vehicle problems the live engine may delete.
     *
     * @return a HashSet of the vehicle problems that may be deleted automatically by the live engine.
     */
    public static HashSet<VehicleProblem> getDeletableProblems() {
        return new HashSet<>(Arrays.asList(VehicleProblem.Accident, VehicleProblem.General));
    }
}
