package de.se.model.mocks;

import de.se.DB.hibernate_models.Timebetweenstopsdb;
import de.se.DB.hibernate_models.Tourdb;
import de.se.data.*;
import de.se.data.Position;
import de.se.model.interfaces.TourStore;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TourStoreMock implements TourStore {

    List<Tour> tours;
    List<Timebetweenstopsdb> timebetweenstopsdbs;
    @Override
    public List<Tour> getToursForLine(Line line) {
       return tours;
    }

    @Override
    public List<Tour> getToursForVehicle(String vehicleID) {
        return tours;
    }


    @Override
    public Tour getCurrentTourForVehicle(String vehicleID) {
        if (vehicleID.equals("V1") || vehicleID.equals("V2")) {
            return tours.get(0);
        }
        return null;
    }

    @Override
    public List<Tour> getCurrentTours(Line line) {
        return  tours;
    }

    @Override
    public List<Tour> getCurrentToursWithDelays(Line line, HashMap<String, Delay> vehicleDelays) {
        return tours;
    }

    @Override
    public List<Tour> getAllExistingTours() {
        return  tours;
    }

    @Override
    public List<Timebetweenstopsdb> getTimebetweenstops() {
        return timebetweenstopsdbs;
    }

    @Override
    public void saveTours(List<Tour> tours) {

    }

    @Override
    public List<String> checkForAvailableVehicles(Date startTime, String lineId) {
        List<String> result = new ArrayList<String>();
        for(Tour tour : tours){
            result.add(tour.getVehicle());
        }

        return result;
    }

    @Override
    public void saveTour(Tour tour) {

    }

    @Override
    public void updateTour(Tour tour, String vehicle) throws Exception {

    }

    @Override
    public void removeTour(Tour tour) {

    }

    @Override
    public void removeTours(List<Tour> tours) {

    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void initialize() {
        tours = new ArrayList<Tour>();

        Timestamp starttime = new Timestamp((new Date()).getTime());
        Timestamp endtime = new Timestamp(starttime.getTime() + 30 * 60 * 1000);
        tours.add(new Tour(1, starttime, endtime,  "one", "two" , "V2" , "l1"));
        tours.add(new Tour(2, starttime ,endtime,  "one", "three" , "V1" , "l1"));
        tours.add(new Tour(3, starttime , endtime,  "one", "four" , "V1" , "l1"));
        tours.add(new Tour(4, starttime ,endtime,  "one", "three" , "V1" , "l1"));

        HashMap<Stop, Date> map = new HashMap<Stop, Date>();
        map.put(new Stop("s1", "here" , "123456", "12345" , null), new Date());
        for(Tour tour : tours){
            tour.setStopTime(map);
        }
    }

    @Override
    public void keepToursUpToDate() {

    }

    @Override
    public Position getPositionFromTour(Tour tour) {
        return new Position(tour.getStartStop(), tour.getEndStop(), true); //this is stupid (just mock)
    }

    @Override
    public Position getPositionFromDelayedTour(Tour tour, int delayInSeconds) {
        return new Position("Prev stop for delay testing", "Next stop for delay testing", false);
    }

    @Override
    public Tour getTourById(int tourId) {
        return tours.get(0);
    }

    @Override
    public Tour getCopiedTourById(int tourId) {
        Tour tour = tours.get(0);
        Tourdb db = tour.getTourDB();

        Tour copy = new Tour(db.getId(), db.getStartTime(), db.getEndTime(), db.getStartStop(), db.getEndStop(), tour.getVehicle(), tour.getLine());
        copy.setStopTime(tour.getStopTime());

        return copy;
    }
    @Override
    public long getEndtimeOfTour(Tour tour) {
        return 0;
    }

    @Override
    public List<Tour> getAssignableToursInTimeRange(String vehicleId, long start, long end) {
        return tours;
    }

    @Override
    public List<String> getPossibleReplacementVehicles(String old) {
        return tours.stream().map(s -> s.getVehicle()).collect(Collectors.toList());
    }

    @Override
    public void setReplacementVehicle(String old, String newVehicle) throws Exception {

    }

    @Override
    public List<Tour> getToursWithoutVehicles() {
        return tours;
    }


}
