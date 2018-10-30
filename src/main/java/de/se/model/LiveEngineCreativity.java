package de.se.model;

import de.se.data.enums.FeedbackEnum;

import java.util.Random;

/**
 * This class stores random texts that will be used as "content" in randomly generated feedbacks
 */
public class LiveEngineCreativity {
    private String[][] lineFeedbackContents = {
            {"This line is always overcrowded :("}, // LineProblem.Overflow
            {"There is something in the way of the Vehicle "}, // LineProblem.Obstacle
            {"It would be nice if you could extend the line",
                    "The vehicles on this line are always really old"} // LineProblem.General
    };
    private String[][] stopFeedbackContents = {
            {"Stop is dirty", "Stop smells"}, //StopProblem.Dirty
            {"The platform is always too full"}, // StopProblem.Overflow
            {"Something does not work here"} // StopProblem.Broken
    };
    private String[][] vehicleFeedbackContents = {
            {"Smoke is coming from the engine", "The Engine makes strange noises"}, // VehicleProblem.Defect_Engine
            {"Vehicle is dirty", "Vehicle smells"}, // VehicleProblem.Dirty
            {"Vehicle is burning"}, // VehicleProblem.Accident
            {"The vehicle is way too full", "There are too many people in here"},
            {"It is way to cool in the Vehicle",
                    "I think that the air conditioner is broken since it is really hot in here"}, // VehicleProblem.Defect_Airconditioner
            {"Door does not open", "The door does not close correctly"}, // VehicleProblem.Defect_Door
            {"Driver is rude", "There is a smelling rumpsteak from Argentina in the vehicle", // VehicleProblem.General
            "Rolf's party is currently taking place in the vehicle (and the music is way too loud)"}
    };
    // can stay a normal array since there are no Enum typs
    private String[] generalFeedbackContents = {"I want to see timetables in Spanish"};

    /**
     *
     * @param type is the type of the FeedbackEnum (Line, Stop, Vehicle, General)
     * @param problemEnumNumber is the number of the Problem type in the problem enums (e.g. for LineProblem.Obstacle -> 1)
     * @return a String consistent to the FeedbackEnum typ and the problem typ
     */
    public String getRandomProblem(FeedbackEnum type, int problemEnumNumber) {
        Random random = new Random();
        switch (type) {
            case Line:
                return lineFeedbackContents[problemEnumNumber][random.nextInt(lineFeedbackContents[problemEnumNumber].length)];
            case Stop:
                return stopFeedbackContents[problemEnumNumber][random.nextInt(stopFeedbackContents[problemEnumNumber].length)];
            case Vehicle:
                return vehicleFeedbackContents[problemEnumNumber][random.nextInt(vehicleFeedbackContents[problemEnumNumber].length)];
            case General:
                return generalFeedbackContents[random.nextInt(generalFeedbackContents.length)];
            default:
                return generalFeedbackContents[random.nextInt(generalFeedbackContents.length)];
        }
    }
}