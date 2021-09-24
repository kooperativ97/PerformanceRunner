package at.hajszan.performancerunner.persistence.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import at.hajszan.performancerunner.converters.DateConverter;
import at.hajszan.performancerunner.persistence.Dao.PerformanceDataItemDao;
import at.hajszan.performancerunner.persistence.Dao.PerformancePlanItemDao;
import at.hajszan.performancerunner.persistence.Dao.PerformanceRunDao;
import at.hajszan.performancerunner.persistence.Entity.PerformanceDataItem;
import at.hajszan.performancerunner.persistence.Entity.PerformancePlanItem;
import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;

@Database(entities = {PerformanceDataItem.class, PerformanceRun.class, PerformancePlanItem.class}, version = 7, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class PRDatabase extends RoomDatabase {

    private static final String DB_NAME = "prdb";
    private static volatile PRDatabase database = null;

    public static synchronized PRDatabase getDatabase(Context context) {
        if (database == null) {
                PRDatabase db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        PRDatabase.class,
                        DB_NAME)
                        //delete everything in case of database upgrade
                        .fallbackToDestructiveMigration()
                        .build();
                database = db;
        }
        return database;
    }

    public abstract PerformanceDataItemDao getPerformanceDataItemDao();
    public abstract PerformanceRunDao getPerformanceRunDao();
    public abstract PerformancePlanItemDao getPerformancePlanItemDao();
}
