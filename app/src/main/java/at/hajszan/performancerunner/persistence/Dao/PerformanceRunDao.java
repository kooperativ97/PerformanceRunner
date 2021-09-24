package at.hajszan.performancerunner.persistence.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;

@Dao
public interface PerformanceRunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addItem(PerformanceRun item);

    @Query("SELECT * FROM performance_run WHERE id = :id")
    PerformanceRun getRun(long id);

    @Query("SELECT * FROM performance_run WHERE name = :name")
    PerformanceRun getRunFromName(String name);

    @Query("SELECT * FROM performance_run")
    List<PerformanceRun> getAll();

    @Query("SELECT * FROM performance_run where id = (select max(id) from performance_run) limit 1")
    PerformanceRun getLatestRun();

    @Query("DELETE FROM performance_run WHERE id = :id")
    void deleteRun(long id);

    @Query("UPDATE performance_run SET name = :name WHERE id = :id")
    void updateName(String name, long id);

}
