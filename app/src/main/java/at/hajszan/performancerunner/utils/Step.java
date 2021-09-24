package at.hajszan.performancerunner.utils;

/**
 * A step encapsulates the point of time of a step and provides functions to check if this step
 * was taken in a given time range.
 */
public class Step {
    private long timestamp;

    public Step(long timestamp) {
        this.timestamp = timestamp;
    }


    public boolean within(long timestamp, int timeWithin) {
        return (timestamp - this.timestamp) < timeWithin;
    }

    public boolean withinLastMinute(long timestamp) {
        return this.within(timestamp, 60000);
    }
}
