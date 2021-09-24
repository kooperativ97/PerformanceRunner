package at.hajszan.performancerunner.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import at.hajszan.performancerunner.R;
import at.hajszan.performancerunner.persistence.Entity.PerformanceDataItem;
import at.hajszan.performancerunner.services.StepDetectorService;
import at.hajszan.performancerunner.utils.ActivityResultIdentifier;
import at.hajszan.performancerunner.utils.PerformancePlan;
import at.hajszan.performancerunner.utils.PerformancePlanIdentifier;
import at.hajszan.performancerunner.utils.StepServiceIdentifier;
import at.hajszan.performancerunner.utils.TimeLabelFormatter;

/**
 * The MainActivity handling playback and live tempo view,
 * choosing of files for workouts and navigation to other activities
 */
public class MainActivity extends AppCompatActivity {

    boolean mBounded;
    private ServiceConnection mConnection;
    private StepDetectorService detectorService;
    private Intent stepServiceInt;
    private Handler handler;

    private TextView bpmField20;
    private ProgressBar progress;
    private TextView timeLeft;
    private TextView timePassed;

    private TextView audioResultingBPMField;

    private DataUpdateReceiver dataUpdateReceiver;

    private Uri uriMusic;

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PerformancePlanIdentifier.UPDATE.getId())) {
                runOnUiThread(() -> {
                    updateFields((PerformanceDataItem) intent.getExtras().getSerializable(StepServiceIdentifier.PERFITEM.getId()));
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();
        activateStepSensor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("SLEEP", "onStart");
    }

    /**
     * Binds and listens actively to the Step Detector Service of Android.
     */
    public void activateStepSensor() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBounded = false;
                detectorService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBounded = true;
                StepDetectorService.LocalBinder mLocalBinder = (StepDetectorService.LocalBinder)service;
                detectorService = mLocalBinder.getServerInstance();

                if(getIntent().getExtras() != null) {
                    PerformancePlan plan = (PerformancePlan) getIntent().getExtras().getSerializable(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId());
                    if(plan  != null) {
                        startPlanning(plan);
                    }
                }
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

    public void initializeFields() {
        bpmField20 = (TextView) findViewById(R.id.tb_bpm20);
        timeLeft = (TextView) findViewById(R.id.timeLeft);
        timePassed = (TextView) findViewById(R.id.timePassed);
        progress = (ProgressBar) findViewById(R.id.progress);
        audioResultingBPMField = (TextView) findViewById(R.id.audio_resulting_bpm);
    }

    public void updateFields(PerformanceDataItem item) {
        bpmField20.setText(item.getSP20() + " SPM");
        audioResultingBPMField.setText(Math.round(item.getBPMAudio() * item.getPlaybackModifier() * 100d) / 100d + " BPM");
        int seconds = (int) (item.getTimestamp() / 1000);
        int duration = detectorService.getAudioManager().getDuration() / 1000;
        progress.setMax(duration);
        progress.setProgress(seconds);
        TimeLabelFormatter formatter = new TimeLabelFormatter();
        timePassed.setText(String.valueOf(formatter.formatLabel(seconds*1000d, true)));
        timeLeft.setText(String.valueOf(formatter.formatLabel(duration*1000, true)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("SLEEP", "onStop");
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(PerformancePlanIdentifier.UPDATE.getId());
        registerReceiver(dataUpdateReceiver, intentFilter);
        Log.d("STATE", "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("STATE", "onRestart");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deactivateStepSensor();
        detectorService.getAudioManager().stopDispatcher();
        detectorService.stopLogging();
        Log.d("STATE", "onDestroy");
    }

    public void onOpenClick(View view)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent,  ActivityResultIdentifier.FileExplorer.getId());
    }

    public void openHistory()
    {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void onPlanClick(View view) {
        startPlanning(null);
    }

    public void startPlanning(PerformancePlan plan) {

        if (detectorService.getAudioManager().dispatcherIsInitialised()) {
            Intent intent = new Intent(this, PerformancePlanningActivity.class);
            if(plan != null) {
                detectorService.setPlan(plan);
                intent.putExtra(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId(), detectorService.getPlan());
                intent.putExtra(PerformancePlanIdentifier.BPM.getId(), plan.getBaseBPM());
                intent.putExtra(PerformancePlanIdentifier.DURATION.getId(), plan.getDuration());
            } else {
                intent.putExtra(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId(), detectorService.getPlan());
                intent.putExtra(PerformancePlanIdentifier.BPM.getId(), detectorService.getBeatExtractor().getEstimatedBeats(128));
                intent.putExtra(PerformancePlanIdentifier.DURATION.getId(), detectorService.getAudioManager().getDuration());
            }
            startActivityForResult(intent, ActivityResultIdentifier.PerformancePlan.getId());
        }
    }

    public void onStartClick(View view)
    {
        if (detectorService.getAudioManager().dispatcherIsInitialised() && !detectorService.isLogging() && detectorService.getPlan() != null) {
            Log.d("MAIN ", "STARTING STEP SENSOR");
            detectorService.startLogging();
            Log.d("MAIN ", "STARTING PLAYBACK");
            detectorService.getAudioManager().startPlaying();
            Log.d("MAIN ", "STARTING DOCUMENTATION");
            detectorService.startNewPerformanceRun();
        }
    }

    public void onStopClick(View view)
    {
        Log.d("MAIN ","STOP RUN");
        detectorService.stopRun();
        Log.d("MAIN ","STARTING STEP SENSOR");
        deactivateStepSensor();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ActivityResultIdentifier.PerformancePlan.getId()) {
            if(data != null) {
                PerformancePlan plan = (PerformancePlan) data.getExtras().getSerializable(PerformancePlanIdentifier.PERFORMANCE_PLAN.getId());
                detectorService.setPlan(plan);
            }
        } else {
            if (requestCode == ActivityResultIdentifier.FileExplorer.getId())
                if (resultCode == RESULT_OK)
                    uriMusic = data.getData();
            try {
                String path = uriMusic.getPath();
                //handle alternative paths like document/primary:Music/Korper/mp3s... to storage/emulated/0/Music....
                if (uriMusic.getPath().contains(":")) {
                    String tmpPath = uriMusic.getPath().split(":")[1];
                    tmpPath = "/storage/emulated/0/" + tmpPath;
                    path = tmpPath;
                }
                if(detectorService.getAudioManager().getAudioPath() == null || !detectorService.getAudioManager().getAudioPath().equals(path)) {
                    detectorService.setPlan(null);
                }
                detectorService.getAudioManager().initDispatcher(path);
                detectorService.getBeatExtractor().detectBeats(path);

                detectorService.setPathToMusic(path);

                startPlanning(null);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage());
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onShowRunsClick(View view) {
        openHistory();
    }
}

