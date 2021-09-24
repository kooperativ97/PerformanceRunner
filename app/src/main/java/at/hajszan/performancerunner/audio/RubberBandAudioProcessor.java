package at.hajszan.performancerunner.audio;

import com.breakfastquay.rubberband.RubberBandStretcher;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;


public class RubberBandAudioProcessor implements AudioProcessor {

    private final RubberBandStretcher rbs;

    public RubberBandAudioProcessor(int sampleRate, double initialTimeRatio, double initialPitchScale){
        int options = RubberBandStretcher.OptionProcessRealTime | RubberBandStretcher.OptionWindowStandard | RubberBandStretcher.OptionStretchElastic;
        rbs = new RubberBandStretcher(sampleRate, 1, options, initialTimeRatio, initialPitchScale);
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[][] input = {audioEvent.getFloatBuffer()};
        rbs.process(input, false);

        int availableSamples = rbs.available();
        while(availableSamples ==0){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            availableSamples = rbs.available();
        }
        float[][] output = {new float[availableSamples]};
        rbs.retrieve(output);
        audioEvent.setFloatBuffer(output[0]);
        return true;
    }

    @Override
    public void processingFinished() {
        rbs.dispose();
    }

    public void setTimeRatio(double d) {
        double factor = 1 - (d - 1);
        rbs.setTimeRatio(factor);

    }

    public void setPitchScale(double d) {
        rbs.setPitchScale(d);
    }
}