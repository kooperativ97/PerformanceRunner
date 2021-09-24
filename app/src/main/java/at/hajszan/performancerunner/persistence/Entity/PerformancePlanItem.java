package at.hajszan.performancerunner.persistence.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "performance_plan_items")
public class PerformancePlanItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ForeignKey(entity = PerformanceRun.class, parentColumns = "id", childColumns = "run_id", onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "run_id")
    private long runId;

    private int duration;
    private int baseBPM;
    private int pStart;
    private int pDuration;
    private double pManipulation;
    private double pOffset;

    public PerformancePlanItem(int duration, int baseBPM, int pStart, int pDuration, double pManipulation, double pOffset, long runId) {
        this.duration = duration;
        this.baseBPM = baseBPM;
        this.pStart = pStart;
        this.pDuration = pDuration;
        this.pManipulation = pManipulation;
        this.pOffset = pOffset;
        this.runId = runId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
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

    public int getPStart() {
        return pStart;
    }

    public void setPStart(int pStart) {
        this.pStart = pStart;
    }

    public int getPDuration() {
        return pDuration;
    }

    public void setPDuration(int pDuration) {
        this.pDuration = pDuration;
    }

    public double getPManipulation() {
        return pManipulation;
    }

    public void setPManipulation(double pManipulation) {
        this.pManipulation = pManipulation;
    }

    public double getPOffset() {
        return pOffset;
    }

    public void setPOffset(double pOffset) {
        this.pOffset = pOffset;
    }
}
