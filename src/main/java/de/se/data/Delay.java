package de.se.data;

public class Delay {
    private Position expectedPosition; //without delay
    private Position actualPosition; //with delay
    private int delay; //in seconds

    public Delay(Position expectedPosition, Position actualPosition, int delay) {
        this.expectedPosition = expectedPosition;
        this.actualPosition = actualPosition;
        this.delay = delay;
    }

    public Position getExpectedPosition() {
        return expectedPosition;
    }

    public void setExpectedPosition(Position expectedPosition) {
        this.expectedPosition = expectedPosition;
    }

    public Position getActualPosition() {
        return actualPosition;
    }

    public void setActualPosition(Position actualPosition) {
        this.actualPosition = actualPosition;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
