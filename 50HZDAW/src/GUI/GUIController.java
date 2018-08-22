package GUI;

import Audio.MixerSetUp;
import Audio.OutputTrack;
import Audio.Track;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Controller class linking the GUI to the mixer and output track
 */
public class GUIController {

    private FXGUIBuilder view;
    private MixerSetUp mixerSetUp;
    private boolean playing;
    private int startPos;

    /**
     * Constructor for the controller
     * @param newView - GUI Builder
     * @param model - Mixer
     * @throws Exception
     */
    public GUIController(FXGUIBuilder newView, MixerSetUp model) throws Exception {

        view = newView;
        mixerSetUp = model;
        playing = false;
    }

    /**
     * Play the combined output of the arrangement window
     * @param start - Starting point
     */
    public void play(int start) {

        startPos = start;
        // Check if currently playing, if it is then do nothing
        if (!playing) {
            playing = true;
            try {
                // Play output in seperate thread
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            mixerSetUp.playOutput(start);
                            playing = false;
                        } catch (Exception e) {
                        }
                    }

                });
                thread.start();

            } catch (Exception e) {
                System.out.println("Couldn't play track");
            }
        } else {
            // do something
        }
    }

    /**
     * Pause the output and save the new position
     * @param start - int
     */
    public void pause(int start) {
        playing = false;
        startPos = start;
        mixerSetUp.pauseOutput(start);
    }

    /**
     * Stop the output and set position to 0
     * @param start
     */
    public void stop(int start) {
        startPos = start;
        playing = false;
        try {
            mixerSetUp.stopOutput(start);
        } catch (NullPointerException e) {
            System.out.println("No track playing");
        }
    }

    /**
     * Remove track from the output
     * @param track - Track
     */
    public void removeTrack(Track track) {
        mixerSetUp.removeTrack(track);
    }


    public boolean addTrack(File file) {
        return true;
    }

    /**
     * Show an "Are you sure" box whenever before closing the program
     * Kill current output and timer when program is closed
     */
    public void closeProgram(){
        // returns True or false from a yes no window
        boolean answer = ConfirmationBox.Display("Quit", "Are you sure you want to quit?");
        if (answer) {
            view.getWindow().close();
            try {
                stop(startPos);
                mixerSetUp.killTimer();
                view.killTimer();

            } catch (NullPointerException e) {}
        }
    }

    /**
     * Export current arangement as wav
     * @param window
     * @throws LineUnavailableException
     */
    public void export(Stage window) throws LineUnavailableException {
        ExportManager EM = new ExportManager();
        File file = EM.exportAsWAV(window);

        OutputTrack output = mixerSetUp.prepareOutput();


        byte[] outputArray = output.addDataForOutput1Export();
        AudioFormat frmt= new AudioFormat(44100,24,2,true,false);
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(outputArray), frmt, outputArray.length);
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
        catch (IOException IOE) {

        }
    }

    /**
     * Set starting position
     * @param start
     */
    public void setStart(int start) {
        startPos = start;
    }



}

