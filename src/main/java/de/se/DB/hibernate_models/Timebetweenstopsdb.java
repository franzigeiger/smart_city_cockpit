package de.se.DB.hibernate_models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(TimebetweenstopsdbPK.class)
public class Timebetweenstopsdb {
    private String startstop;
    private String nextstop;
    private int timeinminutes;

    @Id
    @Column(name = "startstop")
    public String getStartstop() {
        return startstop;
    }

    public void setStartstop(String startstop) {
        this.startstop = startstop;
    }

    @Id
    @Column(name = "nextstop")
    public String getNextstop() {
        return nextstop;
    }

    public void setNextstop(String nextstop) {
        this.nextstop = nextstop;
    }

    @Basic
    @Column(name = "timeinminutes")
    public int getTimeinminutes() {
        return timeinminutes;
    }

    public void setTimeinminutes(int timeinminutes) {
        this.timeinminutes = timeinminutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timebetweenstopsdb that = (Timebetweenstopsdb) o;
        return timeinminutes == that.timeinminutes &&
                Objects.equals(startstop, that.startstop) &&
                Objects.equals(nextstop, that.nextstop);
    }

    @Override
    public int hashCode() {

        return Objects.hash(startstop, nextstop, timeinminutes);
    }
}
