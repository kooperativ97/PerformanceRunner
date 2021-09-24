package at.hajszan.performancerunner.utils;

import java.util.LinkedList;

/**
 * The StepManager takes care of all steps (list of Step) during a workout.
 * It provides functions to calculate the SPM as well as SP60, SP30, SP20, SP10 metrics.
 */
public class StepManager {

    final int MAX_STEPS = 10000000;

    LinkedList<Step> steps;

    public StepManager() {
        steps = new LinkedList<>();
    }

    public LinkedList<Step> getSteps() {
        return steps;
    }

    public synchronized void addStep(Step step) {
        steps.add(step);
        if (steps.size() > this.MAX_STEPS) {
            long currentTime = System.currentTimeMillis();
            this.steps.removeIf(s -> !s.within(currentTime, 60 * 1000 * 10));
            // 60 * 1000 * 10 ==> 10 Minutes in milliseconds
        }
    }

    public int getBPM() {
        long currentTime = System.currentTimeMillis();
        return (int)(this.steps.parallelStream()
                .filter(s -> s.withinLastMinute(currentTime)).count());
    }

    public synchronized int getBPMEstimate(int seconds) {
        long currentTime = System.currentTimeMillis();
        return (int)((60/seconds) * this.steps.parallelStream()
                .filter(s -> s.within(currentTime, seconds*1000)).count());
    }
}
