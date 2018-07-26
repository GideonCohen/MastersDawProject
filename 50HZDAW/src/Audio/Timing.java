package Audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Timing {


    private long start;
    private long finish;
    private long timeStart;
    private long timeElapsed;
    private long minutes;
    private long seconds;
    private long milliseconds;
    private Timer timer;
    private TimerTask timerTaskMillis;
    // private Metronome metronome;
    private boolean isMetronome;
    private boolean timerSwitch;


    public Timing(long start, long finish, boolean isMetronome) {

        //TimeGui timeGui = new TimeGui();

        this.start = start;
        this.finish = finish;
        milliseconds = 0 + start;

        this.isMetronome = isMetronome;
        // try {
        // metronome = new Metronome();
        //}
        //catch (UnsupportedAudioFileException uafe) {
        //  System.out.println(uafe.getMessage());
        //}
        //catch (IOException ioe) {
        //System.out.println(ioe.getMessage());

        //}
        //catch (LineUnavailableException lua) {
        //  System.out.println(lua.getMessage());

        //   }

        timer = new Timer();

    }

    public void getTimerMillis() {

        timerSwitch = true;
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

                if (milliseconds == 0) {
                    // metronome.play();
                    System.out.println("--------------------------------------------------------------------");
                }

                if ((milliseconds != 0) && (milliseconds % 1000 == 0)) {
                    // metronome.play();
                    System.out.println("--------------------------------------------------------------------");
                }


                //  if (milliseconds == (finish - 1)) {    // finish with locators
                //     timer.cancel();
                // }



                System.out.println("millis: " + milliseconds + "  secs: " + seconds + "  mins: " + minutes);

                milliseconds++;


            }

        }

        ;
        timer.scheduleAtFixedRate(timerTaskMillis,0,1);


    }

    /**
     * Stop the timer when pause or stop is pressed.
     */

    public void stop() {
        timer.cancel();
    }

    public long getMillis() { return milliseconds; }

    public long getSeconds() { return seconds; }

    public long getMinutes() { return minutes; }

    public long getFinish() { return finish; }



}




