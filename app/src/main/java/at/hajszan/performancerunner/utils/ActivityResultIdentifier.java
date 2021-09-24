package at.hajszan.performancerunner.utils;

/**
 * Enum to identify Extras in intents
 */
public enum ActivityResultIdentifier {

    PerformancePlan(117),
    FileExplorer(1),
    StepService(2);

    private int id;

    ActivityResultIdentifier(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
