package de.se.DB.mocks;

import de.se.DB.TourPersistence;
import de.se.DB.hibernate_models.Tourdb;
import de.se.data.Stop;
import de.se.data.Tour;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TourPersistenceMock implements TourPersistence {

    static int i =99;
    @Override
    public List<Tour> fetchTours() {
        List<Tour> tours = new ArrayList<Tour>();

        Timestamp starttime = new Timestamp((new Date()).getTime());
        Timestamp endtime = new Timestamp(starttime.getTime() + 3 * 2 * 60 * 1000); // 3 stops, 2min between each
        tours.add(new Tour(1, starttime, endtime,  "one", "four", "V3", "l1"));
        tours.add(new Tour(2, new Timestamp(endtime.getTime()+1000), new Timestamp(endtime.getTime()+5000),  "one", "four", "V3", "l1"));
        tours.add(new Tour(3, starttime, endtime,  "one", "four", "V4", "l1"));
        tours.add(new Tour(4, starttime, endtime,  "one", "four", "V4", "l1"));
        tours.add(new Tour(5, starttime, endtime,  "one", "four", "V5", "l1"));

        HashMap<Stop, Date> map = new HashMap<Stop, Date>();
        map.put(new Stop("s1", "here", "123456", "12345", null), new Date());
        for(Tour tour : tours){
            tour.setStopTime(map);
        }

        return tours;
    }

    @Override
    public void saveTour(Tour tour) {
        if (tour.getId() == -1) {
            tour.getTourDB().setId(getMaxID() + 1);
        }

    }

    @Override
    public void saveTourdbs(List<Tourdb> tourdbs) {
        
    }

    @Override
    public void deleteTour(Tour tour) {

    }

    @Override
    public void updateTour(Tour tour) {

    }

    @Override
    public Tourdb getTourById(int id) {
        return new Tourdb();
    }

    @Override
    public int deleteOlderToursThan(int days) {
        return 0;
    }

    @Override
    public int getNumberOfToursOnDay(Timestamp day) {
        return 0;
    }

    @Override
    public int getMaxID() {
        return i++ +1;
    }
}
