package at.hajszan.performancerunner.audio;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.StopAudioProcessor;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.beatroot.Event;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;

public class BeatExtractor implements OnsetHandler {

    private int estimatedBeats;
    private int beatsFromFileName;

    public BeatExtractor() {
        estimatedBeats = -1;
        beatsFromFileName = -1;
    }

    public void detectBeats(String audio) throws IOException {
        beatsFromFileName = checkIfNameContainsBPM(audio);
        File musicFile = new File(audio);
        int size = 1024;
        int overlap = size/2;
        AudioDispatcher dispatcher  = AudioDispatcherFactory.fromPipe(musicFile.getAbsolutePath(), 44100, size, overlap);
        // getAudioDispatcherFromPipeForSeconds(musicFile.getAbsolutePath(), 44100, size, overlap); //

        // take 5 seconds from the middle of the song
        double start = getDuration(audio)/1000.0/2.0;
        double stop = start + 5;

        Log.d("STARTSTOP", start + " --> " + stop);

        // set the audio dispatcher to the defined starting point
        dispatcher.skip(start);
        // stop after x seconds
        dispatcher.addAudioProcessor(new StopAudioProcessor(stop));

        ComplexOnsetDetector detector = new ComplexOnsetDetector(size, 0.4);
        BeatRootOnsetEventHandler handler = new BeatRootOnsetEventHandler();
        detector.setHandler(handler);
        dispatcher.addAudioProcessor(detector);
        dispatcher.run();

        handler.trackBeats(this);
    }

    private int checkIfNameContainsBPM(String path) {
        Pattern p = Pattern.compile("\\[\\d*([\\sbpmBPM]*)]");
        Matcher m = p.matcher(path);
        if (m.find()) {
            String bpm = path.substring(path.indexOf("["), path.indexOf("."));
            int digits = (int) path.chars().filter(Character::isDigit).count();
            bpm = bpm.substring(1, digits);
            bpm = bpm.replaceAll("\\D*", "");
            Log.d("BPMMM", bpm);
            return Integer.parseInt(bpm);
        }
        return -1;
    }

    private int getDuration(String audio) {
        MediaMetadataRetriever fileMeta = new MediaMetadataRetriever();
        fileMeta.setDataSource(audio);
        String i = fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.e("Duration Check", i);
        return Integer.parseInt(fileMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    private AudioDispatcher getAudioDispatcherFromPipeForSeconds(String path, int sampleRate, int size, int overlap) {
        PipedAudioStream pipedStream = new PipedAudioStream(path);
        TarsosDSPAudioInputStream tarsosInputStream = pipedStream.getMonoStream(sampleRate, 0.0D, 120);
        return new AudioDispatcher(tarsosInputStream, size, overlap);
    }


    public void handleOnset(double time, double salience) {
        Log.d("BEATDETECT ", ""+ time+ " " + salience);
        estimatedBeats = (int) Math.round(time);
    }

    //return bpm stated in title, if not present
    //return estimation (if too far off from fallback value
    //return fallback value
    public int getEstimatedBeats(int fallback) {
        if (beatsFromFileName != -1)
            return beatsFromFileName;
        else if (Math.abs(fallback - estimatedBeats) >= 50)
            return fallback;
        else
            return estimatedBeats;
    }

    /** Creates a new Event object representing an onset or beat.
     *  @param time The time of the beat in seconds
     *  @param beatNum The index of the beat or onset.
     *  @return The Event object representing the beat or onset.
     */
    public static Event newEvent(double time, int beatNum) {
        return new Event(time, time, time, 56, 64, beatNum, 0, 1);
    }

    public void setEstimatedBeats(int estimatedBeats) {
        this.estimatedBeats = estimatedBeats;
    }
}