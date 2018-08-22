package Audio;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer for beats and bars and seconds
 */
public class Timing {


    private long timeStart;
    private long timeElapsed;
    private long minutes;
    private long seconds;
    private long milliseconds;
    private Timer timer;

    private TimerTask timerTaskMillis;
    private boolean timerSwitch;
    private int bars;
    private int beats;
    private int lengthOfBar;
    private int lengthOfBeat;
    private BPMConverter bpmConverter;


    /**
     * Constructor for timing
     */
    public Timing() {

        //TimeGui timeGui = new TimeGui();

        milliseconds = 0;
        bars = 1;
        beats = 1;

        bpmConverter = new BPMConverter();


    }

    public void getTimerMillis(int bpm, Label barsLabel, Label bpmLabel) {


        timer = new Timer();

        bpmConverter.setBPM(bpm);

        // timerSwitch = true;
        timerTaskMillis = new TimerTask() {

            @Override
            public void run() {

                timeStart = System.currentTimeMillis();
                if (milliseconds >= 1000) {
                    seconds = milliseconds / 1000;
                }
                if (seconds % 60 == 0) {             // millisecond accuracy
                    minutes = seconds / 60;
                }

                if (milliseconds >= bpmConverter.setBars(1, bpm)) {
                    bars = (int) (milliseconds / bpmConverter.setBars(1, bpm)) + 1;
                }
                if (milliseconds >= bpmConverter.setBeat(1)) {
                    beats = (int) (milliseconds / bpmConverter.setBeat(1)) + 1;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI here.

                        barsLabel.setText(minutes + ":" + seconds);
                        bpmLabel.setText("bars: " + bars + "  " + "beats: " + +beats + "          ");

                        milliseconds++;

                    }
                });
            }

        };
    }

    /**
     * Start the timer!!
     */

    public void startTimer () {

        timer.scheduleAtFixedRate(timerTaskMillis,0,1);

    }

    /**
     * Stop the timer when stop is pressed.
     */

    public void stopTimer()  {
        timer.cancel();
        timer.purge();
        timerTaskMillis.cancel();
        milliseconds = 0;
        seconds = 0;
        minutes = 0;
        bars = 1;
        beats = 1;

    }

    /**
     * Stop the timer when pause is pressed.
     */

    public void pauseTimer()  {
        timer.cancel();
        timer.purge();
        timerTaskMillis.cancel();


    }


    public void setMillis(int millis) {

        this.milliseconds = millis;
    }


    public long getMillis() { return milliseconds; }

    public long getSeconds() { return seconds; }

    public long getMinutes() { return minutes; }


}

