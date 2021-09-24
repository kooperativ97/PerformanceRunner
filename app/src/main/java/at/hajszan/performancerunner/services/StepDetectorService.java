package at.hajszan.performancerunner.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import at.hajszan.performancerunner.activities.MainActivity;
import at.hajszan.performancerunner.audio.BeatExtractor;
import at.hajszan.performancerunner.audio.StretchAudioManager;
import at.hajszan.performancerunner.persistence.Database.PRDatabase;
import at.hajszan.performancerunner.persistence.Entity.PerformanceDataItem;
import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;
import at.hajszan.performancerunner.utils.PerformancePlan;
import at.hajszan.performancerunner.utils.PerformancePlanIdentifier;
import at.hajszan.performancerunner.utils.Step;
import at.hajszan.performancerunner.utils.StepManager;
import at.hajszan.performancerunner.utils.StepServiceIdentifier;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;

/**
 * This Service handles SensorEvents from the STEP_DETECTION_SERVICE as well as
 * the logging of data to the persistence layer.
 */
public class StepDetectorService extends Service implements SensorEventListener {

    private static final String LOG_TAG = StepDetectorService.class.getName();
    private static final int ONGOING_NOTIFICATION_ID = 1337;
    private StepManager stepManager;
    private IBinder mBinder = new LocalBinder();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) Log.w(LOG_TAG, "Received intent which is null?!");
        }
    };
    private PerformanceRun perfRun;
    private PerformancePlan plan;

    private StretchAudioManager audioManager;
    private BeatExtractor beatExtractor;

    private String pathToMusic;
    private Uri uriMusic;

    private PowerManager.WakeLock wakeLock;

    public static StepDetectorService selfInstance;

    private boolean logging;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public StepDetectorService getServerInstance() {
            return StepDetectorService.this;
        }
    }

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public StepDetectorService() {
        super();
        stepManager = new StepManager();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0 || !logging) return;
        Log.w(LOG_TAG, "Step changed: " + event.values[0]);
        stepManager.addStep(new Step(System.currentTimeMillis()));
        Log.w("RUNNER RUNNER", stepManager.getSteps().size() + " --> " + stepManager.getBPM());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.w(LOG_TAG, "Accuracy changed: " + accuracy);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Creating service.");
        // register for sensors
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(this.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        initializeAudio();
        selfInstance = this;
    }

    static public StepDetectorService getInstance() {
        return selfInstance;
    }

    public PRDatabase getDatabase() {
        return PRDatabase.getDatabase(this.getApplicationContext());
    }

    public void startRun() {
        acquireWakelock();
        addNotification();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                startLogging();
                addLogToDatabase();
            }
        });
        t.start();
    }

    //makes sure the service stays alive
    public void acquireWakelock() {
        PowerManager mgr = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "performanceRunner:stayCounting");
        wakeLock.acquire(15*60*1000L /*20 minutes timeout*/);
    }

    public void addNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 1, notificationIntent, 0);

        Notification notification;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chanId = createNotificationChannel("StepDetectorService", "Step Detector");
            notification =
                    new Notification.Builder(this, chanId)
                            .setContentTitle("Performance Runner")
                            .setContentText("tracking your steps")
                            .setContentIntent(pendingIntent)
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .build();
        }
        else {
            notification =
                    new Notification.Builder(this)
                            .setContentTitle("Performance Runner")
                            .setContentText("tracking your steps")
                            .setContentIntent(pendingIntent)
                            .build();
        }

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    public void stopRun() {
        getAudioManager().stopDispatcher(); //kills cron job too
        stopLogging();
        stopForeground(true);
        if(wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    public void startNewPerformanceRun() {
        AsyncTask.execute(() -> {
            perfRun = new PerformanceRun("Run", pathToMusic, beatExtractor.getEstimatedBeats(120));
            perfRun.setName("Run_"+perfRun.getDate().toString().substring(0, 13));
            long id = getDatabase().getPerformanceRunDao().addItem(perfRun);
            getDatabase().getPerformancePlanItemDao().addItem(plan.toPerformancePlanItem(id));
            perfRun.setId(id);
            perfRun.setTimestamp(System.currentTimeMillis());
            startLogging();
            startRun();
        });
    }


    private void addLogToDatabase(){

        while(!getAudioManager().dispatcherIsStopped()) {

            int runningFor = (int) (System.currentTimeMillis() - perfRun.getTimestamp());
            audioManager.manipulateSpeed(plan.getTempoManipulator(runningFor));

            PerformanceDataItem dataItem = new PerformanceDataItem(
                    perfRun.getId(),
                    runningFor,
                    this.getStepManager().getBPMEstimate(60),
                    this.getStepManager().getBPMEstimate(30),
                    this.getStepManager().getBPMEstimate(20),
                    this.getStepManager().getBPMEstimate(10),
                    beatExtractor.getEstimatedBeats(120),
                    audioManager.getSpeedManipulator());

            Intent n = new Intent(PerformancePlanIdentifier.UPDATE.getId());
            n.putExtra(StepServiceIdentifier.PERFITEM.getId(), dataItem);
            sendBroadcast(n);

            AsyncTask.execute(() -> {
                getDatabase().getPerformanceDataItemDao().addItem(dataItem);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * There are different Sensors for step detection. Use STEP_DETECTOR as it is perfectly for this task.
     *
     * @return Type of sensors requested
     */
    public int getSensorType() {
        return Sensor.TYPE_STEP_DETECTOR;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying service.");
        // Unregister sensor listeners
        stopLogging();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        stopRun();
        super.onDestroy();
    }

    /**
     * Initializes the Audio Features such as the AudioManager as well as the
     * Android FFMPEG Locator.
     */
    public void initializeAudio() {
        audioManager = new StretchAudioManager();
        beatExtractor = new BeatExtractor();
        AndroidFFMPEGLocator ffmpeg = new AndroidFFMPEGLocator(this);
    }

    public void startLogging() {
        this.logging = true;
    }

    public void stopLogging() {
        this.logging = false;
    }

    public StepManager getStepManager() {
        return this.stepManager;
    }

    public PerformanceRun getPerfRun() {
        return perfRun;
    }

    public PerformancePlan getPlan() {
        return plan;
    }

    public StretchAudioManager getAudioManager() {
        return audioManager;
    }

    public BeatExtractor getBeatExtractor() {
        return beatExtractor;
    }

    public String getPathToMusic() {
        return pathToMusic;
    }

    public Uri getUriMusic() {
        return uriMusic;
    }

    public void setPathToMusic(String pathToMusic) {
        this.pathToMusic = pathToMusic;
    }

    public void setUriMusic(Uri uriMusic) {
        this.uriMusic = uriMusic;
    }

    public void setPlan(PerformancePlan plan) {
        this.plan = plan;
    }

    public boolean isLogging() {
        return logging;
    }
}