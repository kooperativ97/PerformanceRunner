package at.hajszan.performancerunner.persistence.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "performance_run")
public class PerformanceRun implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    private String name;
    private Date date;
    private String audioPath;
    private int originalBPM;
    private long timestamp;

    public PerformanceRun(String name, String audioPath, int originalBPM) {
        this.name = name;
        this.audioPath = audioPath;
        this.originalBPM = originalBPM;
        this.date = new Date();
        this.timestamp = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getOriginalBPM() {
        return originalBPM;
    }

    public void setOriginalBPM(int originalBPM) {
        this.originalBPM = originalBPM;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
