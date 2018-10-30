package de.se.data;

public class Position {

    private String prevStop;
    private String nextStop;
    private boolean onPrev;

    public Position(String prevStop, String nextStop, boolean onPrev) {
        this.prevStop = prevStop;
        this.nextStop = nextStop;
        this.onPrev = onPrev;
    }

    public String getPrevStop() {
        return prevStop;
    }

    public void setPrevStop(String prevStop) {
        this.prevStop = prevStop;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    public boolean isOnPrev() {
        return onPrev;
    }

    public void setOnPrev(boolean onPrev) {
        this.onPrev = onPrev;
    }
}
