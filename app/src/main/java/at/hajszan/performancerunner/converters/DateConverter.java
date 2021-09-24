package at.hajszan.performancerunner.converters;
import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Converts timestamps to dates and dates to timestamps
 */
public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
