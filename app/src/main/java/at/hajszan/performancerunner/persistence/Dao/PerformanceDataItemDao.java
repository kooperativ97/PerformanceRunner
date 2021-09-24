package at.hajszan.performancerunner.persistence.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.hajszan.performancerunner.persistence.Entity.PerformanceDataItem;

@Dao
public interface PerformanceDataItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addItem(PerformanceDataItem item);

    @Query("SELECT * FROM performance_data_items WHERE run_id = :run")
    List<PerformanceDataItem> getItemsOfRun(long run);

    @Query("DELETE FROM performance_data_items WHERE run_id = :run")
    void deleteRunItems(long run);
}
