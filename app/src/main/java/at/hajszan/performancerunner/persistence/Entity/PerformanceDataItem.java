package at.hajszan.performancerunner.persistence.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "performance_data_items")
public class PerformanceDataItem  implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ForeignKey(entity = PerformanceRun.class, parentColumns = "id", childColumns = "run_id", onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "run_id")
    private long runId;

    private long timestamp;
    private int SP60;
    private int SP30;
    private int SP20;
    private int SP10;
    private int BPMAudio;
    private double playbackModifier;

    public PerformanceDataItem(long runId, long timestamp, int SP60, int SP30, int SP20, int SP10, int BPMAudio, double playbackModifier) {
        this.runId = runId;
        this.timestamp = timestamp;
        this.SP60 = SP60;
        this.SP30 = SP30;
        this.SP20 = SP20;
        this.SP10 = SP10;
        this.BPMAudio = BPMAudio;
        this.playbackModifier = playbackModifier;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSP60() {
        return SP60;
    }

    public void setSP60(int SP60) {
        this.SP60 = SP60;
    }

    public int getSP30() {
        return SP30;
    }

    public void setSP30(int SP30) {
        this.SP30 = SP30;
    }

    public int getSP20() {
        return SP20;
    }

    public void setSP20(int SP20) {
        this.SP20 = SP20;
    }

    public int getSP10() {
        return SP10;
    }

    public void setSP10(int SP10) {
        this.SP10 = SP10;
    }

    public int getBPMAudio() {
        return BPMAudio;
    }

    public void setBPMAudio(int BPMAudio) {
        this.BPMAudio = BPMAudio;
    }

    public double getPlaybackModifier() {
        return playbackModifier;
    }

    public void setPlaybackModifier(double playbackModifier) {
        this.playbackModifier = playbackModifier;
    }
}
