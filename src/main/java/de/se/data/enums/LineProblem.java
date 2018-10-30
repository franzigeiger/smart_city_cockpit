package de.se.data.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public enum LineProblem {

    Overflow, Obstacle, General;

    /**
     * Picks a random value of the LineProblem enum
     * @return a random LineProblem
     */
    public static LineProblem getRandomLineProblem() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }

    public static LineProblem getLineProblemByNumber(int enumNumber) {
        switch (enumNumber) {
            case 0:
                return LineProblem.Overflow;
            case 1:
                return LineProblem.Obstacle;
            default:
                return LineProblem.General;
        }
    }

    /**
     * Not every type of problem may be deleted by the live engine because not every problem can be solved externally.
     * This method determines the types of line problems the live engine may delete.
     * @return a HashSet of the line problems that may be deleted automatically by the live engine.
     */
    public static HashSet<LineProblem> getDeletableProblems() {
        return new HashSet<>(Arrays.asList(LineProblem.Overflow, LineProblem.Obstacle, LineProblem.General));
    }
}
