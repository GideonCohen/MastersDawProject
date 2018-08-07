package Audio;

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



    public static void main (String [] args) {

        MixerSetUp mixerSetUp = new MixerSetUp(0);   // set I/0 preferences

    }



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


/*        addTrack("Track1",-0.5f);     // create mixer with one track as default.
        addTrack("Track1", 0.1f);     // create mixer with one track as default.
        addTrack("Track1", 0.1f);     // create mixer with one track as default.

        System.out.println("tracks: " + tracks.size());

        output.setMute(0);   // mute a track.
        output.setMute(1);
        output.setMute(2);
        output.setSolo(0);
        output.setSolo(1);
        output.setSolo(2);

        System.out.println("\nTracks in project: " + trackCount());    // TEST TO SEE LINES IN MIXER CORRESPONDS TO ADD TRACK.
*/

/*
        try {
            testOutput();
        } catch (LineUnavailableException e) {

        }
*/

    }

    public void testOutput() throws LineUnavailableException{
        // Testing files
        String filepath_24bit = "50HZDAW/Samples/emotionalpads.wav";
        String hello16bit = "50HZDAW/Samples/Hello16bit.wav";
        String filepath_16bit = "50HZDAW/Samples/doublebass.wav";
        String filepath1_24bit = "50HZDAW/Samples/loopy3.wav";

        File file = new File(filepath_16bit);
        File file1 = new File(filepath_24bit);
        File file2 = new File(filepath1_24bit);
        File file3 = new File(hello16bit);

        // For testing without the GUI
        //addTrack("Track1", file3, 0.8f, 0);     // create mixer with one track as default.
        //addTrack("Track1", file2, 0.8f, 10000);     // create mixer with one track as default.
        System.out.println("tracks: " + tracks.size());
        System.out.println("\nTracks in project: " + trackCount());    // TEST TO SEE LINES IN MIXER CORRESPONDS TO ADD TRACK.
        playOutput();
    }

    /**
     * Add a new track to the mixer. Specify parameter for type of track set-up.
     * @param name - The name of the track
     * @param file - File to add to the track
     * @param volume - volume modifier (>1 increase volume, <1 decrease volume).
     * @return - The Track created
     */
    public Track addTrack (String name, File file, float volume, long start) {

        Track track = null;

        try {
            track = new Track(name,  volume);
            tracks.add(track);
            track.addAudioTrackData(file, start);
        }
        catch (LineUnavailableException lue) {
            System.out.println(lue.getMessage());
        }

        trackCount ++;
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

    public void playOutput () throws LineUnavailableException{

        output = new OutputTrack("OutPut", playOffset);
        System.out.println("Mixer offset is " + playOffset);

        //Add each track to the combined output
        for (Track track: tracks) {
            output.addToOutput(track);
        }

        try {
            output.playTrack();
            //tracks.get(0).playTrack();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Stop the current output and save the current position as the offset
     */
    public void pauseOutput (){
        playOffset = output.pause();

    }

    /**
     *  Stop the current ouput and set the offset as 0
     */
    public void stopOutput (){
        playOffset = output.stop();
    }

    public boolean isPlaying(){
        try {
            return output.isPlaying();
        } catch (NullPointerException e){
            System.out.println("nothing is playing");
            return false;
        }
    }


    public OutputTrack prepareOutput() throws LineUnavailableException {

        output = new OutputTrack("OutPut", playOffset);
        System.out.println("Mixer offset is " + playOffset);

        //Add each track to the combined output
        for (Track track: tracks) {
            output.addToOutput(track);
        }

        return output;
    }


}
