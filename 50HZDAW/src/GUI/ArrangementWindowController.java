package GUI;

import Audio.MixerSetUp;
import Audio.Track;

import java.io.File;
import java.util.ArrayList;


public class ArrangementWindowController {

    private JavaFXController view;
    private MixerSetUp mixerSetUp;

    public ArrangementWindowController(JavaFXController newView, MixerSetUp model) throws Exception {

        view = newView;
        mixerSetUp = model;
    }

    public void play() {
        try {
            view.getTT().play();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        mixerSetUp.playOutput();
                    } catch (Exception e)  {}
                }
            });
            thread.start();
        } catch (Exception e) {
            System.out.println("Couldn't play track");
        }
    }

    public void pause() {
        mixerSetUp.pauseOutput();
    }

    public void stop() {
        mixerSetUp.stopOutput();
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
        boolean answer = ConfirmationBox.Display("", "Are you sure you want to quit?");
        if (answer) {
            view.getWindow().close();
        }
    }


}

