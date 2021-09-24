package at.hajszan.performancerunner.persistence.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import at.hajszan.performancerunner.persistence.Entity.PerformancePlanItem;

@Dao
public interface PerformancePlanItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addItem(PerformancePlanItem item);

    @Query("SELECT * FROM performance_plan_items WHERE run_id = :run")
    PerformancePlanItem getPlanOfRun(long run);

    @Query("DELETE FROM performance_plan_items WHERE run_id = :run")
    void deletePlanItem(long run);
}
