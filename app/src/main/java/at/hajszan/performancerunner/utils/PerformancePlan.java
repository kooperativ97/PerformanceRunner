package at.hajszan.performancerunner.utils;

import java.io.Serializable;

import at.hajszan.performancerunner.persistence.Entity.PerformancePlanItem;

/**
 * The PerformancePlan encapsulates all parameters to calculate the tempo variations as
 * configured by the user.
 *
 */
public class PerformancePlan implements Serializable {
    private int duration;
    private int baseBPM;
    private int pStart;
    private int pDuration;
    private double pManipulation;
    private double pOffset;

    public PerformancePlan(int duration, int baseBPM, int pStart, int pDuration, double pManipulation, double pOffset) {
        this.duration = duration;
        this.baseBPM = baseBPM;
        this.pStart = pStart;
        this.pDuration = pDuration;
        this.pManipulation = pManipulation;
        this.pOffset = pOffset;
    }

    public int getpStart() {
        return pStart;
    }

    public void setpStart(int pStart) {
        this.pStart = pStart;
        if(pStart + pDuration >= 100) {
            pDuration = 100 - pStart;
        }
    }

    public int getpDuration() {
        return pDuration;
    }

    public void setpDuration(int pDuration) {
        this.pDuration = pDuration;
        if(pStart + pDuration >= 100) {
            pStart = 100 - pDuration;
        }
    }

    public double getpOffset() {
        return pOffset;
    }

    public void setpOffset(double pOffset) {
        this.pOffset = pOffset;
    }

    public double getpManipulation() {
        return pManipulation;
    }

    public void setpManipulation(double pManipulation) {
        this.pManipulation = pManipulation;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBaseBPM() {
        return baseBPM;
    }

    public void setBaseBPM(int baseBPM) {
        this.baseBPM = baseBPM;
    }

    public int pointAt(int percent) {
        return (int) (duration / 100 * percent);
    }

    /**
     * calculates the tempo manipulator factor for the audio dispatcher.
     * @param time timestamp of time passed since workout started
     * @return factor of time manipulation (rounded to 3 digits)
     */
    public double getTempoManipulator(int time) {
        if (time < duration * ((double) pStart / 100)) // BEFORE INCREASE
            return  roundWithPrecision(pOffset, 3);
        else if (time > duration * (((double) pStart + (double) pDuration) / 100)) // AFTER INCREASE
            return roundWithPrecision(pOffset * pManipulation, 3);
        else // DURING INCREASE
        {
            double startOfIncrease = (((double) pStart) / 100 * duration);
            double lengthOfIncrease = (((double) pDuration) / 100 * duration);

            double at = time - startOfIncrease;
            double atModifier = at / lengthOfIncrease;
            return roundWithPrecision(((1 + ((pManipulation - 1) * atModifier)) * pOffset), 3);
        }
    }

    public void setOffset(double offset) {
        this.pOffset = offset;
    }

    private double roundWithPrecision(double value, int digits) {
        return (double) Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
    }

    public static PerformancePlan fromPerformancePlanItem(PerformancePlanItem item) {
        return new PerformancePlan(item.getDuration(),
                item.getBaseBPM(),
                item.getPStart(),
                item.getPDuration(),
                item.getPManipulation(),
                item.getPOffset());
    }

    public PerformancePlanItem toPerformancePlanItem(long id) {
        return new PerformancePlanItem(this.getDuration(),
                this.getBaseBPM(),
                this.getpStart(),
                this.getpDuration(),
                this.getpManipulation(),
                this.getpOffset(),
                id);
    }

}

