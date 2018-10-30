package de.se.DB.hibernate_models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class TimebetweenstopsdbPK implements Serializable {
    private String startstop;
    private String nextstop;

    @Column(name = "startstop")
    @Id
    public String getStartstop() {
        return startstop;
    }

    public void setStartstop(String startstop) {
        this.startstop = startstop;
    }

    @Column(name = "nextstop")
    @Id
    public String getNextstop() {
        return nextstop;
    }

    public void setNextstop(String nextstop) {
        this.nextstop = nextstop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimebetweenstopsdbPK that = (TimebetweenstopsdbPK) o;
        return Objects.equals(startstop, that.startstop) &&
                Objects.equals(nextstop, that.nextstop);
    }

    @Override
    public int hashCode() {

        return Objects.hash(startstop, nextstop);
    }
}
