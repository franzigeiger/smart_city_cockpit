package de.se.data.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * This enum contains problems, which can appear at a stop
 */
public enum StopProblem {
    Overflow, Dirty, Broken;

    /**
     * Picks a random value of the StopProblem enum
     * @return a random StopProblem
     */
    public static StopProblem getRandomStopProblem() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }

    public static StopProblem getStopProblemByNumber(int enumNumber) {
        switch (enumNumber) {
            case 0:
                return StopProblem.Overflow;
            case 1:
                return StopProblem.Dirty;
            default:
                return StopProblem.Broken;
        }
    }

    /**
     * Not every type of problem may be deleted by the live engine because not every problem can be solved externally.
     * This method determines the types of stop problems the live engine may delete.
     * @return a HashSet of the stop problems that may be deleted automatically by the live engine.
     */
    public static HashSet<StopProblem> getDeletableProblems() {
        return new HashSet<>(Arrays.asList(StopProblem.Overflow));
    }
}
