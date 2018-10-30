package de.se.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the route of a line
 */
public class Route {

    private List<Stop> stopSequence;
    //something destroys the order of stopSequence from time to time therefore we need a backup to repair it
    private List<Stop> stopSequenceBackUp;

    public Route(List<Stop> stopSequence) {
        this.stopSequence = stopSequence;
        stopSequenceBackUp = new ArrayList<>();
        stopSequenceBackUp.addAll(stopSequence);
    }

    public List<Stop> getStopSequence() {
        stopSequence = new ArrayList<>(stopSequenceBackUp); //renew always to prevent changing
        return stopSequence;
    }

}
