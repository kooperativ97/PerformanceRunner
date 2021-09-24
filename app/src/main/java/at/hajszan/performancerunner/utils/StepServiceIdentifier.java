package at.hajszan.performancerunner.utils;

/**
 * Enum to identify Extras in intents
 */
public enum StepServiceIdentifier {

    AUDIOMANAGER("AudioManager"),
    BEATEXTRACTOR("BeatExtractor"),
    PLAN("PerformancePlan"),
    PERFRUN("PerformanceRun"),
    PERFITEM("PerformanceRunItem");

    private String id;

    StepServiceIdentifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
