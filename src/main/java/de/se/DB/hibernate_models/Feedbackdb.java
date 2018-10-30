package de.se.DB.hibernate_models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class Feedbackdb {
    private int id;
    private String content;
    private Timestamp commitTime;
    private boolean finished;
    private String reason;
    private String placetype;
    private String placeinstance;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "commit_time")
    public Timestamp getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Timestamp commitTime) {
        this.commitTime = commitTime;
    }

    @Basic
    @Column(name = "finished")
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Basic
    @Column(name = "reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Basic
    @Column(name = "placetype")
    public String getPlacetype() {
        return placetype;
    }

    public void setPlacetype(String placetype) {
        this.placetype = placetype;
    }

    @Basic
    @Column(name = "placeinstance")
    public String getPlaceinstance() {
        return placeinstance;
    }

    public void setPlaceinstance(String placeinstance) {
        this.placeinstance = placeinstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feedbackdb that = (Feedbackdb) o;

        if (id != that.id) return false;
        if (finished != that.finished) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (commitTime != null ? !commitTime.equals(that.commitTime) : that.commitTime != null) return false;
        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        if (placetype != null ? !placetype.equals(that.placetype) : that.placetype != null) return false;
        if (placeinstance != null ? !placeinstance.equals(that.placeinstance) : that.placeinstance != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (commitTime != null ? commitTime.hashCode() : 0);
        result = 31 * result + (finished ? 1 : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (placetype != null ? placetype.hashCode() : 0);
        result = 31 * result + (placeinstance != null ? placeinstance.hashCode() : 0);
        return result;
    }
}
