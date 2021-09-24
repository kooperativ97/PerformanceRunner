package at.hajszan.performancerunner.utils;

/**
 * Enum to identify Extras in intents
 */
public enum PerformancePlanIdentifier {

    BPM("baseBPM"),
    PERFORMANCE_PLAN("PerformancePlan"),
    DURATION("Duration"),
    UPDATE("Update");

    private String id;

    PerformancePlanIdentifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
