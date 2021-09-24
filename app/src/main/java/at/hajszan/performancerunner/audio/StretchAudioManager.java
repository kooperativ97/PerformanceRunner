package at.hajszan.performancerunner.audio;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.MultichannelToMono;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;



public class StretchAudioManager {

    String audioPathThis;

    private static final String LOG_TAG = StretchAudioManager.class.getName();

    private RubberBandAudioProcessor rubberBandProcessor;
    private String audioPathOut;
    private AudioDispatcher dispatcher;
    private WaveformSimilarityBasedOverlapAdd wsola;
    private GainProcessor gainProcessor;
    private int durationPercent;
    private File musicFile;
    private boolean dispatcherInit = false;
    private double speedManipulator = 1;


    public void initDispatcher(String audio) {
        if(dispatcher != null){
            dispatcher.stop();
        }

        this.setAudioPath(audio);
        Log.d("AUDIO_MANAGER"," = " + audioPathThis);
        musicFile = new File(audioPathThis); //uriPath.getPath()
        rubberBandProcessor = new RubberBandAudioProcessor(44100, speedManipulator, 1d);

        dispatcher = AudioDispatcherFactory.fromPipe(musicFile.getAbsolutePath(), 44100,  4096, 0);

        dispatcher.addAudioProcessor(new MultichannelToMono(1,true));
        dispatcher.addAudioProcessor(new GainProcessor(0.5));
        dispatcher.addAudioProcessor(rubberBandProcessor);
        dispatcher.addAudioProcessor(new GainProcessor(1.5));
        dispatcher.addAudioProcessor(new AndroidAudioPlayer(dispatcher.getFormat(),11518, AudioManager.STREAM_MUSIC));

        dispatcherInit = true;
    }

    public void initWSOLA(String audio) {
        if(dispatcher != null){
            dispatcher.stop();
        }

        this.setAudioPath(audio);
        Log.d("AUDIO_MANAGER"," = " + audioPathThis);
        musicFile = new File(audioPathThis);
        wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(1, 44100));
        dispatcher = AudioDispatcherFactory.fromPipe(musicFile.getAbsolutePath(), 44100,  wsola.getInputBufferSize(), wsola.getOverlap());
        dispatcher.addAudioProcessor(new MultichannelToMono(1,true));
        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        dispatcher.addAudioProcessor(new AndroidAudioPlayer(dispatcher.getFormat(),5000, AudioManager.STREAM_MUSIC));
        dispatcherInit = true;
    }

    public void readMetadata() {
        MediaMetadataRetriever fileMeta = new MediaMetadataRetriever();
        fileMeta.setDataSource(musicFile.getAbsolutePath());
        durationPercent = Math.abs(Integer.parseInt(fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000);
        fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    public int getDuration() {
        MediaMetadataRetriever fileMeta = new MediaMetadataRetriever();
        fileMeta.setDataSource(musicFile.getAbsolutePath());
        String i = fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.e("Duration Check", i);
        return Integer.parseInt(fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public void startPlaying() {
        Thread audioThread = new Thread(dispatcher);
        audioThread.start();
    }

    public void stopDispatcher() {
        if(dispatcherInit)
            dispatcher.stop();
        dispatcherInit = false;
    }

    public void manipulateSpeed(double by) {
        if (by != this.speedManipulator) {
            Log.i("Music", "updating speedManipulator from " + this.speedManipulator + " to " + by);
            this.speedManipulator = by;
            //wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(this.speedManipulator, 44100));
            rubberBandProcessor.setTimeRatio(by);
        }
    }

    public double getSpeedManipulator() {
        return speedManipulator;
    }

    public void runDispatcher() {
        dispatcher.run();
    }

    public boolean dispatcherIsStopped() {
        return dispatcher.isStopped();
    }

    public boolean dispatcherIsInitialised() {
        return dispatcherInit;
    }

    public String getAudioPathOut() {
        return audioPathOut;
    }

    public String getAudioPath() {
        return audioPathThis;
    }

    public void setAudioPath(String audioPathInput) {
        audioPathThis = audioPathInput;
    }

    public void setAudioPathOut(String audioPathOut) {
        audioPathOut = audioPathOut;
    }
}
