package de.se.data;

import de.se.DB.hibernate_models.Tourdb;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all information about a tour.
 * It enriches TourDB objects with additional information to produce Tour objects.
 */
public class Tour {

    private Tourdb tourDB;



    Map<Stop, Date> stopTime;

    public Tour(int id, Timestamp startTime, Timestamp endTime, String startStop, String endStop,
                String vehicle, String line) {
        this.tourDB= new Tourdb();
        this.tourDB.setId(id);
        this.tourDB.setStartTime(startTime);
        this.tourDB.setEndTime(endTime);
        this.tourDB.setStartStop(startStop);
        this.tourDB.setEndStop(endStop);
        this.tourDB.setVehicle(vehicle);
        this.tourDB.setLine(line);
    }

    public Tour(Tourdb tourDB) {
        this.tourDB = tourDB;
        stopTime= new HashMap<Stop, Date>();
    }

    public Tourdb getTourDB() {
        return tourDB;
    }

    public int getId() {
        return tourDB.getId();
    }

    public Date getStartTime() {
        return tourDB.getStartTime();
    }

    public Date getEndTime() {
        return tourDB.getEndTime();
    }

    public String getStartStop() {
        return tourDB.getStartStop();
    }

    public String getEndStop() {
        return tourDB.getEndStop();
    }

    public String getVehicle() {
        return tourDB.getVehicle();
    }

    public void setVehicle(String vehicelId){
        tourDB.setVehicle(vehicelId);
    }

    public String getLine() {
        return tourDB.getLine();
    }

    public Map<Stop, Date> getStopTime() {
        return stopTime;
    }

    public void setStopTime(Map<Stop, Date> stopTime) {
        this.stopTime = stopTime;
    }

    public void setEndTime(Timestamp endtime) {
        this.tourDB.setEndTime(endtime);
    }
}
