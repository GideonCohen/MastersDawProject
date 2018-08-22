package Audio;

import javafx.animation.TranslateTransition;
import javafx.scene.shape.Rectangle;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.util.ArrayList;

/**
 *  MixerSetUp class represents a 32 channel mixer than may consist of one or or more lines of data.
 *  A mixer will be set up with one single data line for audio data and additional lines (aka channels/tracks)
 *  may be added to the audio system.
 */


public class MixerSetUp {


    // The Mixer
    private Mixer mixer;
    // Array of mixer information
    private Mixer.Info [] mixInfos;
    // List of all tracks currently in the mixer
    private ArrayList <Track> tracks;
    // Array of lines in the mixer
    private Line [] lines;
    // Combination of all other tracks to be played
    private OutputTrack output;
    // Number of tracks in the mixer
    private int trackCount;
    // Starting position of playback
    private long playOffset;
    // BPM for the project
    private int bpm;
    // Timeline TT
    private TranslateTransition TT;

    // Start in ms.
    private int startPos;

    private boolean timerOn;

    private Rectangle r;


    /**
     * Create a mixer object with a given i/o choice from mixinfos. This may be changed in preferences as a later date.
     * @param audioPreferences - I/O preferences
     * @throws LineUnavailableException
     */
    public MixerSetUp(int audioPreferences) {

        mixInfos = AudioSystem.getMixerInfo();   // get the list of available i/o (built-in/soundcard etc).

        mixer = AudioSystem.getMixer(mixInfos[audioPreferences]);  // assign our mixer to a chosen i/o system.

        trackCount = 0;

        tracks = new ArrayList<>();

        bpm = 120;    // CHANGE BPM HERE FOR TRACK TIMELINE & BEATS AND BAR CHANGES!
        timerOn = true;


    }

    /**
     * Set start pos of project.
     */

    public void setStart(int start) {

        this.startPos = start;
    }

    /**
     * Get the start position of the track.
     */

    public int getStart() {

        return this.startPos;
    }


    /**
     * Add a new track to the mixer. Specify parameter for type of track set-up.
     * @param name - The name of the track
     * @param file - File to add to the track
     * @return - The Track created
     */
    public Track addTrack (String name, File file, long start) {

        Track track = null;

        try {
            track = new Track(name);
            tracks.add(track);
            track.addAudioTrackData(file, start);
        } catch (LineUnavailableException lue) {
            System.out.println(lue.getMessage());
        }

        trackCount++;
        System.out.println("Tracks in output: " + tracks.size());
        lines = mixer.getSourceLines();  // refresh amount of lines in mixer.

        return track;
    }


    /**
     * Remove a given Track
     * @param track - the Track to be removed
     */
    public void removeTrack (Track track) {
        tracks.remove(track);
        trackCount --;

        //Update the lines
        lines = mixer.getSourceLines();
    }


    /**
     * Keep a count of the tracks in the form of an array of lines.
     * @return the number of open lines in a mixer.
     */

    public int trackCount () {

        lines = mixer.getSourceLines();

        return lines.length;
    }


    /**
     * Play all tracks together.
     */

    public void playOutput (int start) throws LineUnavailableException{

        setStart(start);

        output = new OutputTrack("OutPut", startPos);
        System.out.println("Mixer offset is " + startPos);

        //Add each track to the combined output
        for (Track track: tracks) {
            output.addToOutput(track);
        }

        try {
            TT.play();
            output.playTrack();
            //tracks.get(0).playTrack();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Stop the current output and save the current position as the offset
     */
    public void pauseOutput (int start){
        setStart(start);
        TT.pause();
        playOffset = output.pause();
        System.out.println("Mixer offset is " + startPos);

    }



    /**
     *  Stop the current ouput and set the offset as 0
     */
    public void stopOutput (int start){

        setStart(start);
        playOffset = output.stop();
        //System.out.println("Mixer offset is " + startPos);
        TT.stop();
        r.setTranslateX(0);
    }

    public boolean isPlaying(){
        try {
            return output.isPlaying();
        } catch (NullPointerException e){
            System.out.println("nothing is playing");
            return false;
        }
    }


    /**
     * prepare the output track to be played
     * @return Output track
     * @throws LineUnavailableException
     */
    public OutputTrack prepareOutput() throws LineUnavailableException {

        output = new OutputTrack("OutPut", startPos);

        //Add each track to the combined output
        for (Track track: tracks) {
            output.addToOutput(track);
        }

        return output;
    }

    /**
     * get the current play offset
     * @return long play offset
     */
    public long getPlayOffset() {
        return playOffset;
    }

    /**
     * get current play position of the output
     * @return long play offset
     */
    public long getCurrentPosition() {
        return output.getCurrentPosition();
    }
    /**
     * Get the bpm of the project.
     */
    public int getBpm() {

        return bpm;
    }

    /**
     * kill the timers
     */
    public void killTimer () {

        timerOn = false;
    }

    /**
     * return true is the timer is on
     * @return boolean
     */
    public boolean getTimerStatus() {

        return timerOn;
    }

    /**
     * Set the bpm of the project.
     */
    public int setBpm(int newBpm) {

        this.bpm = newBpm;
        return this.bpm;
    }

    /**
     * set the animation for the timer
     * @param t - Translate Transition - animation for the pointer
     */
    public void setTT(TranslateTransition t) {
        TT = t;
    }

    /**
     * Set the node for the pointer of the tracking line
     * @param rect - Rectangle tracking line node
     */
    public void setRectangle(Rectangle rect) {
        r = rect;
    }

}
