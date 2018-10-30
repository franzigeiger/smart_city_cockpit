package de.se.model;

import de.se.DB.hibernate_models.*;
import de.se.data.*;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleEnum;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class has the sole purpose of generating some dummy objects for test purposes
 */
public class DummyTestObjectGenerator {

    public static Linedb getDummyLineDB() {
        Linedb linedb = new Linedb();
        linedb.setId("bakerloo");
        linedb.setName("Bakerloo");
        linedb.setColor("c28d5b");
        linedb.setType("tube");

        return linedb;
    }

    public static Event getDummyEvent() {
        Date startTimestamp = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTimestamp);
        calendar.add(Calendar.HOUR, 2);
        return new Event(9999, "Big soccer match", startTimestamp,
                new Timestamp(calendar.getTime().getTime()), "Wembley Stadium",
                "Fancy description", "Arsenal Football Club"); //involvedParty has to be a real party from AppointmentInvolvedParties class
    }

    public static Feedback getDummyFeedback() {
        Feedbackdb db = new Feedbackdb();
        db.setId(9999);
        db.setContent("Random content");
        db.setCommitTime(new Timestamp(new Date().getTime()));
        db.setFinished(false);
        db.setReason("We will delete this feedback right away");
        db.setPlacetype("vehicle");
        db.setPlaceinstance("T_055");
        return new Feedback(db);
    }

    public static Vehicledb getDummyVehicledb() {
        Vehicledb vehicledb = new Vehicledb();
        vehicledb.setId("testVehicle");
        vehicledb.setType(VehicleEnum.Tube.toString());
        vehicledb.setDeleted(false);

        return vehicledb;
    }

    public static Timebetweenstopsdb getDummyTimebetweenstopsdb() {
        Timebetweenstopsdb timebetweenstopsdb = new Timebetweenstopsdb();
        timebetweenstopsdb.setTimeinminutes(100);
        timebetweenstopsdb.setStartstop("LUHAW");
        timebetweenstopsdb.setNextstop("LUBKG");
        //real stations but not real connection so it does not overwrite correct data
        return timebetweenstopsdb;
    }

    public static Tourdb getDummyTourDB() {
        Tourdb tourdb = new Tourdb();
        tourdb.setId(9999999);
        tourdb.setVehicle(null); //must reference a real VehicleDB (e.g. Vehicle_0) or be null
        tourdb.setLine("bakerloo"); //must reference a real LineDB
        tourdb.setStartStop("LUHAW"); //must reference a real StopDB
        tourdb.setEndStop("LUEAC"); //must reference a real StopDB
        tourdb.setStartTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        // makes no sense to end it at the same time but does not matter
        tourdb.setEndTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        return tourdb;
    }

    //only works for mock
    public static Tourdb getDummyTourDBForMock() {
        Tourdb tourdb = new Tourdb();
        tourdb.setId(9999999);
        tourdb.setVehicle("V1");
        tourdb.setLine("l1");
        tourdb.setStartStop("one");
        tourdb.setEndStop("two");
        tourdb.setStartTime(new Timestamp(Calendar.getInstance().getTimeInMillis()+100000));
        // makes no sense to end it at the same time but does not matter
        tourdb.setEndTime(new Timestamp(Calendar.getInstance().getTimeInMillis()+120000));
        return tourdb;
    }

    public static Tourdb getDummyTourDBForMock2() {
        Tourdb tourdb = new Tourdb();
        tourdb.setId(9999999);
        tourdb.setVehicle("V2"); //must reference a real VehicleDB (e.g. Vehicle_1) or be null
        tourdb.setLine("l1"); //must reference a real LineDB
        tourdb.setStartStop("one"); //must reference a real StopDB
        tourdb.setEndStop("two"); //must reference a real StopDB
        tourdb.setStartTime(new Timestamp(Calendar.getInstance().getTimeInMillis()+100000000));
        // makes no sense to end it at the same time but does not matter
        tourdb.setEndTime(new Timestamp(Calendar.getInstance().getTimeInMillis()+120000000));
        return tourdb;
    }

    // only works for mocked classes: is on stop three
    public static Tour getCurrentlyRunningTourOnStop() {
        Date now = new Date();
        Timestamp start = new Timestamp(now.getTime() - 2 * 2 * 60 * 1000);
        Timestamp end = new Timestamp(start.getTime() + 104 * 2 * 60 * 1000);
        Tour tour = new Tour(9999999, start, end, "one", "four", "Vehicle_0", "l1");

        return tour;
    }

    // only works for mocked classes: runs between three and four
    public static Tour getCurrentlyRunningTourBetweenStops() {
        Date now = new Date();
        Timestamp start = new Timestamp(now.getTime() -  2 * 2 * 60 * 1000 - 31 * 1000);
        Timestamp end = new Timestamp(start.getTime() + 104 * 2 * 60 * 1000);
        Tour tour = new Tour(9999999, start, end, "one", "four", "Vehicle_0", "l1");

        return tour;
    }

    // only works for mocked classes
    public static Tour getCurrentlyRunningTourOnLastStop() {
        Date now = new Date(); // there are 104 stops in the mock class taking each 2 minutes
        Timestamp start = new Timestamp(now.getTime() - 104 * 2 * 60 * 1000);
        Timestamp end = new Timestamp(now.getTime());
        Tour tour = new Tour(9999999, start, end, "one", "ID 99", "Vehicle_0", "l1");

        return tour;
    }

    // only works for mocked classes
    public static Tour getOldTour() {
        Date now = new Date();
        Timestamp start = new Timestamp(now.getTime() - 90 * 60 * 1000);
        Timestamp end = new Timestamp(start.getTime() + 30 * 60 * 1000);
        Tour tour = new Tour(9999999, start, end, "one", "four", "Vehicle_0", "l1");

        return tour;
    }

    // only works for mocked classes
    public static Tour getUpcomingTour() {
        Date now = new Date();
        Timestamp start = new Timestamp(now.getTime() +  60 * 60 * 1000);
        Timestamp end = new Timestamp(start.getTime() + 49 * 60 * 1000);
        Tour tour = new Tour(9999999, start, end, "one", "four", "Vehicle_0", "l1");

        return tour;
    }

    public static Tour getTourWithoutEndTime () {
        Date now = new Date();
        Timestamp start = new Timestamp(now.getTime());
        Tour tour = new Tour(9999999, start, null, "one", "four", "Vehicle_0", "l1");

        return tour;
    }

    public static ServiceRequest getServiceRequest() {
        Stop stop = new Stop("Stop1");
        stop.setDescription("Random stop description");
        List<TimedStopProblem> stopProblems = new ArrayList<>();
        stopProblems.add(new TimedStopProblem(StopProblem.Broken));
        stop.setProblems(stopProblems);
        Date date = new Date();
        date.setTime(date.getTime() + 3 * (long) 8.64e+7); //3 days in the future
        return new ServiceRequest(99999, 1, date, date, "Random description",
                stop, ServiceRequest.PROCESSING_TYPE_CODE.SRRQ, "1", "Stop", "Random SR name");
    }

    // for not mocked class
    public static Tourdb get100DaysOldTour() {
        Date now = new Date();

        Tourdb tourdb = new Tourdb();
        tourdb.setId(9999999);
        tourdb.setVehicle("T_055"); //must reference a real VehicleDB (e.g. T_055) or be null
        tourdb.setLine("bakerloo"); //must reference a real LineDB
        tourdb.setStartStop("LUHAW"); //must reference a real StopDB
        tourdb.setEndStop("LUEAC"); //must reference a real StopDB

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -100);
        Timestamp timestampStart = new Timestamp(cal.getTimeInMillis());
        tourdb.setStartTime(timestampStart);
        tourdb.setEndTime(new Timestamp(tourdb.getStartTime().getTime() + 39 * 60 * 1000));

        return tourdb;
    }
}
