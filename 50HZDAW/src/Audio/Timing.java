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
    private Metronome metronome;
    private boolean isMetronome;
    private boolean timerSwitch;
    private int bars;
    private int beats;
    private int lengthOfBar;
    private int lengthOfBeat;





    public Timing(long start, long finish, boolean isMetronome) {

        //TimeGui timeGui = new TimeGui();

        this.start = start;
        this.finish = finish;
        milliseconds = 0 + start;
        bars = 0;
        beats = 0;

        this.isMetronome = isMetronome;
        try {
           metronome = new Metronome();
        }
        catch (UnsupportedAudioFileException uafe) {
            System.out.println(uafe.getMessage());
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());

        }
        catch (LineUnavailableException lua) {
           System.out.println(lua.getMessage());

        }

        timer = new Timer();

    }

    public void getTimerMillis(float bpm) {

        if(bpm >= 120) {
            float offSet = bpm / 60;
            lengthOfBar = (int) (1000 * (2 + (2 - offSet)));      // convert bars & beats in to millisecond timing.
            lengthOfBeat = lengthOfBar / 4;                       // calculations for when tempo is increased
        }
        if(beats < 60) {
            float offSet = bpm / 60;
            lengthOfBeat = (int) (1000 * (1 + (1- offSet)));      // convert bars & beats in to millisecond timing.
            lengthOfBar = lengthOfBeat * 4;                      // calculation for when tempo is decreased
        }

        System.out.println(lengthOfBar);
        System.out.println(lengthOfBeat);

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
                    try {
                        metronome.play(bpm);
                    }
                    catch(LineUnavailableException lue) {
                        System.out.println(lue.getMessage());
                    }
                    catch(IOException io) {
                        System.out.println(io.getMessage());
                    }
                    System.out.println("--------------------------------------------------------------------");
                }



                if ((milliseconds != 0) && ((milliseconds % lengthOfBar) == 0)) {   // generate bars.

                        System.out.println("--------------------------------------------------------------------");
                        bars ++;
                        System.out.println("BAR COUNT: " + bars);


                }

                    if ((milliseconds != 0) && ((milliseconds % lengthOfBeat) == 0)) {   // generate beats in bar (4/4)

                        try {
                            metronome.play(bpm);
                            System.out.println("--------------------------------------------------------------------");
                            beats ++;
                            System.out.println("BEAT COUNT: " + beats);
                        }
                        catch(LineUnavailableException lue) {
                            System.out.println(lue.getMessage());
                        }
                        catch(IOException io) {
                            System.out.println(io.getMessage());
                        }

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

    public void stop() throws LineUnavailableException, IOException {
        metronome.stop();
        }

    public long getMillis() { return milliseconds; }

    public long getSeconds() { return seconds; }

    public long getMinutes() { return minutes; }

    public long getFinish() { return finish; }



}





