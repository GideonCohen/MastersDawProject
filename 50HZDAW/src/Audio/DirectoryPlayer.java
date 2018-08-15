package Audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class DirectoryPlayer {


   private Long trackPosition;
   private AudioInputStream audioInputStream;
   private  Clip clip;

    public DirectoryPlayer() {
        trackPosition = 0L;
        audioInputStream = null;
        clip = null;
    }

    /**
     * Loads clips into input stream. Will stop clip if one is already loaded.
     * @param file
     */

    public void loadClip(File file) {
        try {
            if (clip != null) {
                stop();
            }
            audioInputStream = AudioSystem.getAudioInputStream(file.getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (Exception e) {
            System.out.println("Some exception");
            e.printStackTrace();
        }
    }


    public void play() {
        if (clip != null) {
            clip.setMicrosecondPosition(trackPosition);
            clip.start();
            System.out.println("Played clip");
        }
    }


        public void pause() {
        if (clip != null) {
            trackPosition = clip.getMicrosecondPosition();
            clip.stop();
            System.out.println("Paused clip");
        }
    }

        public void stop() {
            if (clip != null) {
                clip.stop();
                trackPosition = 0L;
                System.out.println("Stopped Clip");
            }
        }
    }

