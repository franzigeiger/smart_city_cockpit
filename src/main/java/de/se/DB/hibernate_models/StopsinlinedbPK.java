package de.se.DB.hibernate_models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class StopsinlinedbPK implements Serializable {
    private String line;
    private String stop;

    @Column(name = "line")
    @Id
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Column(name = "stop")
    @Id
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
        StopsinlinedbPK that = (StopsinlinedbPK) o;
        return Objects.equals(line, that.line) &&
                Objects.equals(stop, that.stop);
    }

    @Override
    public int hashCode() {

        return Objects.hash(line, stop);
    }
}
