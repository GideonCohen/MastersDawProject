package GUI;

import Audio.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;


public class GUIController {

    private FXGUIBuilder view;
    private MixerSetUp mixerSetUp;
    private boolean playing;

    public GUIController(FXGUIBuilder newView, MixerSetUp model) throws Exception {

        view = newView;
        mixerSetUp = model;
        playing = false;
    }

    public void play() {
        if (!playing) {
            playing = true;
            try {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            mixerSetUp.playOutput();
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

    public void pause() {
        playing = false;
        mixerSetUp.pauseOutput();
    }

    public void stop() {
        playing = false;
        try {
            mixerSetUp.stopOutput();
        } catch (NullPointerException e) {
            System.out.println("No track playing");
        }
    }

    public void removeTrack(Track track) {
        mixerSetUp.removeTrack(track);
    }

    public boolean addTrack(File file) {

        return true;
    }

    /**
     * Show an "Are you sure" box whenever before closing the program
     */
    public void closeProgram(){
        // returns True or false from a yes no window
        boolean answer = ConfirmationBox.Display("Quit", "Are you sure you want to quit?");
        if (answer) {
            view.getWindow().close();
            try {
                stop();
                view.getTT().stop();
                view.getTiming().stopTimer();
            } catch (NullPointerException e) {}
        }

    }

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


}
