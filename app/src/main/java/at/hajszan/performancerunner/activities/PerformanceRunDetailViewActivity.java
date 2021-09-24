package at.hajszan.performancerunner.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import at.hajszan.performancerunner.R;
import at.hajszan.performancerunner.persistence.Database.PRDatabase;
import at.hajszan.performancerunner.persistence.Entity.PerformanceDataItem;
import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;
import at.hajszan.performancerunner.services.StepDetectorService;
import at.hajszan.performancerunner.utils.PerformanceMetric;
import at.hajszan.performancerunner.utils.PerformancePlan;
import at.hajszan.performancerunner.utils.PerformancePlanIdentifier;
import at.hajszan.performancerunner.utils.TimeLabelFormatter;

/**
 * This Activity provides a more detailed view of a past run with information about the
 * run (name, bpm, path, date) as well as the performance data visualized.
 */
public class PerformanceRunDetailViewActivity extends AppCompatActivity implements TextWatcher {

    private EditText nameText;
    private TextView audioBPMText;
    private TextView audioPath;
    private TextView dateText;
    private GraphView graph;
    private PerformanceRun run;

    private boolean mBounded;
    private ServiceConnection mConnection;
    private StepDetectorService detectorService;
    private Intent stepServiceInt;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activateStepSensor();
        setContentView(R.layout.activity_performance_run_detail_view);
        nameText = (EditText) findViewById(R.id.debugInfo);
        audioBPMText = (TextView) findViewById(R.id.audio_base_bpm_text);
        audioPath = (TextView) findViewById(R.id.audio_path);
        dateText = (TextView) findViewById(R.id.dateField);
        nameText.addTextChangedListener(this);
        graph = (GraphView) findViewById(R.id.graph);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        long extra = intent.getLongExtra("PerformanceRunId", 0);

        AsyncTask.execute(() -> {
            PerformanceRun run = getDatabase().getPerformanceRunDao().getRun(extra);
            this.run = run;

        });

        class CustomAsyncTask extends AsyncTask<Void, Void, PerformanceRun> {

            @Override
            protected PerformanceRun doInBackground(Void... voids) {
                PerformanceRun run = getDatabase().getPerformanceRunDao().getRun(extra);
                return run;
            }

            protected void onPostExecute(PerformanceRun result) {
                nameText.setText(run.getName());
                audioBPMText.setText(String.valueOf(run.getOriginalBPM()));
                String text = run.getAudioPath();
                if(text.length() >= 60) {
                    audioPath.setText("..." + text.substring(Math.max(0, text.length()-60)));
                } else {
                    audioPath.setText(text);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateText.setText(run.getDate().toInstant().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            .replace("T", " ")
                            .substring(0, 16).replace("-", "."));
                } else {
                    dateText.setText(run.getDate().toString());
                }

                showGraphForLastRun(run);
            }
        }

        new CustomAsyncTask().execute(null, null, null);
    }

    public PRDatabase getDatabase() {
        return PRDatabase.getDatabase(this.getApplicationContext());
    }

    public void showGraphForLastRun(PerformanceRun run) {
        AsyncTask.execute(() -> {
            LineGraphSeries<DataPoint> points10 = getPointsForRun(run.getId(), PerformanceMetric.SP10);
            points10.setColor(Color.GREEN);
            points10.setThickness(2);
            points10.setTitle("SP10");
            LineGraphSeries<DataPoint> points30 = getPointsForRun(run.getId(), PerformanceMetric.SP30);
            points30.setColor(Color.BLUE);
            points30.setThickness(2);
            points10.setTitle("SP30");
            LineGraphSeries<DataPoint> points_song = getPointsForRun(run.getId(), PerformanceMetric.BPM_Original);
            points_song.setColor(Color.RED);
            points_song.setThickness(3);
            points10.setTitle("Reference");
            runOnUiThread(() -> {
                graph.removeAllSeries();
                graph.setTitle(run.getName());
                graph.addSeries(points10);
                graph.addSeries(points30);
                graph.addSeries(points_song);
                graph.getGridLabelRenderer().setLabelFormatter(new TimeLabelFormatter());
                graph.getViewport().setMaxX(30000);
                graph.getViewport().scrollToEnd();
                graph.getViewport().setScrollable(true);
                graph.getViewport().setScalable(true);
                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setMaxY(run.getOriginalBPM() * 1.30);
                graph.getViewport().setMinY(run.getOriginalBPM() * 0.70);
                graph.getGridLabelRenderer().setVerticalAxisTitle("BPM/SPM");
                graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
                graph.setLegendRenderer(new LegendRenderer(graph));
            });
            graph.setLegendRenderer(new LegendRenderer(graph));
        });
    }

    public LineGraphSeries<DataPoint> getPointsForRun(long runId, PerformanceMetric performanceMetric) {
        List<PerformanceDataItem> steps = getDatabase().getPerformanceDataItemDao().getItemsOfRun(runId);
        return new LineGraphSeries<>(steps.stream().map(s -> new DataPoint(s.getTimestamp(), getRightBPM(s, performanceMetric))).toArray(DataPoint[]::new));
    }

    private double getRightBPM(PerformanceDataItem item, PerformanceMetric performanceMetric) {
        switch (performanceMetric) {
            case SP10: return item.getSP10();
            case SP20: return item.getSP20();
            case SP30: return item.getSP30();
            case SP60: return item.getSP60();
            case BPM_Original: return item.getBPMAudio() * 1d * item.getPlaybackModifier();
        }
        return item.getSP60();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deactivateStepSensor();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = nameText.getText().toString();
        AsyncTask.execute(() -> {
            getDatabase().getPerformanceRunDao().updateName(text, run.getId());
        });
    }

    public void onDeleteClick(View view) {
        AsyncTask.execute(() -> {
            getDatabase().getPerformanceRunDao().deleteRun(run.getId());
        });
        onBackPressed();
    }

    public void onRedoClick(View view) {
        AsyncTask.execute(() -> {
            PerformancePlan plan = PerformancePlan.fromPerformancePlanItem(getDatabase().getPerformancePlanItemDao().getPlanOfRun(run.getId()));

            String path = run.getAudioPath();

            if(detectorService.getAudioManager().getAudioPath() == null || !detectorService.getAudioManager().getAudioPath().equals(path)) {
                detectorService.setPlan(null);
            }
            detectorService.getAudioManager().initDispatcher(path);
            try {
                detectorService.getBeatExtractor().detectBeats(path);
            } catch (IOException e) {
                Log.e("PerformanceRunDetail", e.getMessage());
            }

            detectorService.setPathToMusic(path);

            if (detectorService.getAudioManager().dispatcherIsInitialised()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId(), plan);
                intent.putExtra(PerformancePlanIdentifier.BPM.getId(), run.getOriginalBPM());
                intent.putExtra(PerformancePlanIdentifier.DURATION.getId(), plan.getDuration());
                startActivity(intent);
            }

        });
    }

    public void activateStepSensor() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // Toast.makeText(MainActivity.this, "StepListener deactivated", Toast.LENGTH_SHORT).show();
                mBounded = false;
                detectorService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // Toast.makeText(MainActivity.this, "StepListener activated", Toast.LENGTH_SHORT).show();
                mBounded = true;
                StepDetectorService.LocalBinder mLocalBinder = (StepDetectorService.LocalBinder)service;
                detectorService = mLocalBinder.getServerInstance();
            }
        };
        stepServiceInt = new Intent(this, StepDetectorService.class);
        bindService(stepServiceInt, mConnection, BIND_AUTO_CREATE);
        handler = new Handler();
    }

    public void deactivateStepSensor() {
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }


}