package de.se.data;

import de.se.DB.hibernate_models.Feedbackdb;
import de.se.data.enums.FeedbackEnum;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This class contains a feedback of a user of the smart city,
 */
public class Feedback {

    private Feedbackdb feedbackdb;

    public Feedback(int id, String content, Date timeStamp, boolean finished, String reason,
                    FeedbackEnum type, String placeInstance) {
        //type is Vehicle, Stop, Line or General and placeInstance references an actual object, e.g. the vehicle "T_001"
        this.feedbackdb = new Feedbackdb();
        this.feedbackdb.setId(id);
        this.feedbackdb.setContent(content);
        this.feedbackdb.setCommitTime(new Timestamp(timeStamp.getTime()));
        this.feedbackdb.setFinished(finished);
        this.feedbackdb.setReason(reason);
        this.feedbackdb.setPlacetype(type.toString());
        this.feedbackdb.setPlaceinstance(placeInstance);
    }

    public Feedback(Feedbackdb feedbackdb) {
        this.feedbackdb = feedbackdb;
    }

    public Feedbackdb getFeedbackdb() {
        return feedbackdb;
    }

    public int getId() {
        return feedbackdb.getId();
    }

    public void setId(int id) {
        this.feedbackdb.setId(id);
    }

    public String getContent() {
        return feedbackdb.getContent();
    }

    public Date getTimeStamp() {
        return feedbackdb.getCommitTime();
    }

    public boolean getFinished() {
        return feedbackdb.isFinished();
    }

    public void setFinished(boolean bool){
        feedbackdb.setFinished(bool);
    }

    public String getReason() {
        return feedbackdb.getReason();
    }

    public FeedbackEnum getType() {
        switch (feedbackdb.getPlacetype()) {
            case "Vehicle":
                return FeedbackEnum.Vehicle;
            case "Stop":
                return FeedbackEnum.Stop;
            case "Line":
                return FeedbackEnum.Line;
            default:
                return FeedbackEnum.General;
        }
    }

    public String getPlaceInstance() {
        return this.feedbackdb.getPlaceinstance();
    }

}
