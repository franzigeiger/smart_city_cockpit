package de.se.data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This class contains the data for an event.
 */
public class Event {

    private int id;
    private String title;
    private Timestamp startTimestamp;
    private Timestamp endTimestamp;
    private String location;
    private String description;
    private String sapObjectID; //the object ID that SAP assigns to this object -> we need this for deleting stuff from the SAP database
    private String involvedParty;

    public Event(int id, String title, Date startTimestamp, Date endTimestamp, String location,
                 String description, String involvedParty) {
        this.id = id;
        this.title = title;
        this.startTimestamp = new Timestamp(startTimestamp.getTime());
        this.endTimestamp = new Timestamp(endTimestamp.getTime());
        this.location = location;
        this.description = description;
        this.involvedParty = involvedParty;
    }

    public Event() {
        this.id = -1;
        this.title = "";
        this.startTimestamp = null;
        this.endTimestamp = null;
        this.location = "";
        this.description = "";
        this.involvedParty = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Timestamp getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Timestamp endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSapObjectID() {
        return sapObjectID;
    }

    public void setSapObjectID(String sapObjectID) {
        this.sapObjectID = sapObjectID;
    }

    public String getInvolvedParty() {
        return involvedParty;
    }

    public void setInvolvedParty(String involvedParty) {
        this.involvedParty = involvedParty;
    }
}
