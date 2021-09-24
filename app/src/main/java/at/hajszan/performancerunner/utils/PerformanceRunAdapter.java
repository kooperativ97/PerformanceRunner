package at.hajszan.performancerunner.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import at.hajszan.performancerunner.R;
import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;

/**
 * This Adapter creates list view elements for each run
 */
public class PerformanceRunAdapter extends BaseAdapter {

    private Activity context;
    private List<PerformanceRun> runs;
    private LayoutInflater inflater;


    public PerformanceRunAdapter(Activity context, List<PerformanceRun> runs) {
        this.context = context;
        this.runs = runs;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<PerformanceRun> getRuns() {
        return runs;
    }

    public void setRuns(List<PerformanceRun> runs) {
        this.runs = runs;
    }

    @Override
    public int getCount() {
        return runs.size();
    }

    @Override
    public Object getItem(int i) {
        return runs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            View v = convertView;
            v = (convertView == null) ? inflater.inflate(R.layout.list_item, null) : v;

            TextView header = (TextView) v.findViewById(R.id.textViewName);
            TextView description = (TextView) v.findViewById(R.id.textViewInfo);
            TextView bpm = (TextView) v.findViewById(R.id.BPMText);

            PerformanceRun selectedPerf = runs.get(position);

            header.setText(selectedPerf.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                description.setText(selectedPerf.getDate().toInstant().atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .replace("T", " ")
                        .substring(0, 16).replace("-", "."));
            } else {
                description.setText(selectedPerf.getDate().toString());
            }
            bpm.setText(selectedPerf.getOriginalBPM() + " BPM");


            return v;
        } catch (Exception ex) {
            Log.e("PerformanceRunAdapter", "error", ex);
            return null;
        }
    }
}
