package at.hajszan.performancerunner.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;
import java.util.List;

import at.hajszan.performancerunner.R;
import at.hajszan.performancerunner.utils.ActivityResultIdentifier;
import at.hajszan.performancerunner.utils.PerformancePlan;
import at.hajszan.performancerunner.utils.PerformancePlanIdentifier;
import at.hajszan.performancerunner.utils.TimeLabelFormatter;

/**
 * Activity used to plan tempo variations in music with 4 sliders. The tempo variations are visualized
 * by a plot shown at the top.
 */
public class PerformancePlanningActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    PerformancePlan plan;
    TextView musicInfo;
    TextView currentValue;
    GraphView graph;

    SeekBar manipulationSeekBar;
    SeekBar durationSeekBar;
    SeekBar startSeekBar;
    SeekBar offsetSeekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.performance_planning);
        plan = (PerformancePlan) getIntent().getExtras().getSerializable(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId());
        if(plan  == null) {
            plan = new PerformancePlan(
                    getIntent().getExtras().getInt(PerformancePlanIdentifier.DURATION.getId()),
                    getIntent().getExtras().getInt(PerformancePlanIdentifier.BPM.getId()),
                    10, 30, 1.04, 1 // pStart,  pDuration,  pManipulation
            );
        }

        Toast.makeText(this, plan.getBaseBPM() + ": for" + plan.getDuration(), Toast.LENGTH_LONG);
        initFields();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initFields() {
        musicInfo = (TextView) findViewById(R.id.musicInfo);
        currentValue = (TextView) findViewById(R.id.currentValue);

        graph = (GraphView) findViewById(R.id.graph);
        loadGraph();

        manipulationSeekBar = (SeekBar) findViewById(R.id.modifierSeek);
        durationSeekBar = (SeekBar) findViewById(R.id.durationSeek);
        startSeekBar = (SeekBar) findViewById(R.id.startSeek);
        offsetSeekBar = (SeekBar) findViewById(R.id.offsetSeek);

        refreshSliders();

        manipulationSeekBar.setOnSeekBarChangeListener(this);
        durationSeekBar.setOnSeekBarChangeListener(this);
        startSeekBar.setOnSeekBarChangeListener(this);
        offsetSeekBar.setOnSeekBarChangeListener(this);
    }

    private void refreshSliders() {
        manipulationSeekBar.setProgress((int) (Math.abs(0.85 - plan.getpManipulation()) * 100));
        durationSeekBar.setProgress(plan.getpDuration());
        startSeekBar.setProgress(plan.getpStart());
    }

    private LineGraphSeries getPoints() {
        double modifier = plan.getpManipulation();
        int start = plan.getpStart();
        int length = plan.getpDuration();

        List<DataPoint> dataPoints = new LinkedList<>();
        dataPoints.add(new DataPoint(0, plan.getBaseBPM() * plan.getpOffset()));
        dataPoints.add(new DataPoint(plan.pointAt(start), (int) plan.getBaseBPM() * plan.getpOffset()));
        dataPoints.add(new DataPoint(plan.pointAt(start + length), (int) (plan.getBaseBPM() * modifier * plan.getpOffset())));
        dataPoints.add(new DataPoint(plan.pointAt(100), (int) (plan.getBaseBPM() * modifier * plan.getpOffset())));

        LineGraphSeries graph = new LineGraphSeries(dataPoints.stream().toArray(DataPoint[]::new));

        graph.setColor(Color.argb(255,  117, 172, 16));

        if(modifier < 1)
            graph.setColor(Color.argb(255, 117, 96, 234));

        if(modifier == 1)
            graph.setColor(Color.argb(255, 255, 246, 238));

        if(modifier >= 1.04)
            graph.setColor(Color.argb(255, 235, 150, 5));

        if(modifier >= 1.08)
            graph.setColor(Color.argb(255, 206, 52, 0));

        return graph;
    }

    private void loadGraph() {
        LineGraphSeries lgs = getPoints();

        runOnUiThread(() -> {
            graph.removeAllSeries();
            graph.setTitle("Progression of Tempo");
            graph.addSeries(lgs);
            graph.onDataChanged(false, false);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(plan.getBaseBPM() * 1.15);
            graph.getViewport().setMinY(plan.getBaseBPM() * 0.85);
            graph.getViewport().setMaxX(plan.pointAt(100));
            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);
            graph.getGridLabelRenderer().setLabelFormatter(new TimeLabelFormatter());
            graph.getGridLabelRenderer().setVerticalAxisTitle("BPM");
            graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        });
    }

    private void alterGraph() {
        LineGraphSeries lgs = getPoints();

        runOnUiThread(() -> {
            graph.removeAllSeries();
            graph.setTitle("Progression of Tempo");
            graph.addSeries(lgs);
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.modifierSeek: plan.setpManipulation(0.85 + (((double)progress )/ 100)); currentValue.setText((int) roundWithPrecision(plan.getpManipulation() * 100 - 100, 2) + " % "); break;
            case R.id.startSeek: plan.setpStart(progress); currentValue.setText(plan.getpStart() + " % "); break;
            case R.id.durationSeek: plan.setpDuration(progress); currentValue.setText(plan.getpDuration() + " % ");  break;
            case R.id.offsetSeek: plan.setOffset(0.85 + (((double) progress )/ 100)); currentValue.setText((int) roundWithPrecision(plan.getpOffset() * 100 - 100,2) + " % "); break;
        }
        alterGraph();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.modifierSeek:  currentValue.setText((int) roundWithPrecision(plan.getpManipulation()* 100 - 100, 2) + " % "); break;
            case R.id.startSeek: currentValue.setText(plan.getpStart() + " % "); break;
            case R.id.durationSeek: currentValue.setText(plan.getpDuration() + " % ");  break;
            case R.id.offsetSeek: currentValue.setText((int) roundWithPrecision(plan.getpOffset() * 100 - 100, 2) + " % "); break;
        }
    }

    private double roundWithPrecision(double value, int digits) {
        return (double) Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        currentValue.setText("");
    }

    public void onHardClick(View view) {
        this.plan.setpDuration(10);
        this.plan.setpStart(10);
        this.plan.setpManipulation(1.08);
        refreshSliders();
        currentValue.setText("");
    }

    public void onMediumClick(View view) {
        this.plan.setpDuration(20);
        this.plan.setpStart(20);
        this.plan.setpManipulation(1.04);
        refreshSliders();
        currentValue.setText("");
    }

    public void onLightClick(View view) {
        this.plan.setpDuration(30);
        this.plan.setpStart(30);
        this.plan.setpManipulation(1.02);
        refreshSliders();
        currentValue.setText("");
    }

    public void doneClick(View view) {
        Intent data = new Intent(this, MainActivity.class);
        data.putExtra(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId(), plan);
        setResult(ActivityResultIdentifier.PerformancePlan.getId(), data);
        finish();
    }


}
