package de.se.DB.hibernate_models;

import javax.persistence.*;

@Entity
@IdClass(StopsinlinedbPK.class)
public class Stopsinlinedb {
    private String line;
    private String stop;
    private Integer positionstoponline;

    @Id
    @Column(name = "line")
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Id
    @Column(name = "stop")
    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stopsinlinedb that = (Stopsinlinedb) o;

        if (line != null ? !line.equals(that.line) : that.line != null) return false;
        if (stop != null ? !stop.equals(that.stop) : that.stop != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = line != null ? line.hashCode() : 0;
        result = 31 * result + (stop != null ? stop.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "positionstoponline")
    public Integer getPositionstoponline() {
        return positionstoponline;
    }

    public void setPositionstoponline(Integer positionstoponline) {
        this.positionstoponline = positionstoponline;
    }
}
