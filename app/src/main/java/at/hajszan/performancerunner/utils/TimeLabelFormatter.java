package at.hajszan.performancerunner.utils;

import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;

/**
 * Formatter to translate timestamps to mm:ss
 */
public class TimeLabelFormatter implements LabelFormatter {

    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            int[] vals = splitToComponentTimes(Math.round(value / 1000));
            return vals[1] + ":" + String.format("%02d", vals[2])  + "";
        }
        return "" + value;
    }

    @Override
    public void setViewport(Viewport viewport) {

    }

    public static int[] splitToComponentTimes(double num)
    {
        int hours = (int) num / 3600;
        int remainder = (int) num - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return new int[]{hours , mins , secs};
    }
}
