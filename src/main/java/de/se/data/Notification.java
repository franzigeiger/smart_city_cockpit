package de.se.data;

import de.se.DB.hibernate_models.Notificationdb;

/**
 * This class contains a message, which can be shown on a stop in case of a problem
 * or to show actual information.
 */
public class Notification {
    private Notificationdb db;
    private Line line;
    private Stop stop;

    public Notification(int id, String description, Stop stop, Line line) {
        this.db = new Notificationdb();
        this.db.setId(id);
        this.db.setDescription(description);
        this.db.setStop(stop == null ? null : stop.getId());
        this.db.setLine(line == null ? null : line.getId());
        this.line=line;
        this.stop=stop;
    }

    public Notification(Notificationdb db) {
        this.db = db;
    }


    public String getDescription() {
        return this.db.getDescription();
    }

    public Stop getTargetStop() {
        return this.stop;
    }

    public Line getTargetLine() {
        return this.line;
    }

    public int getID(){return this.db.getId();}

    public void setID(int id){this.db.setId(id);}

    public Notificationdb getDb() {
        return this.db;
    }

    public String getTargetStopID() {
        return this.db.getStop();
    }

    public String getTargetLineID() {
        return this.db.getLine();
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }
}
