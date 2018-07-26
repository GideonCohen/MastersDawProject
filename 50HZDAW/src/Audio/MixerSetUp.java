package Audio;

import javax.sound.sampled.*;
import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *  MixerSetUp class represents a 32 channel mixer than may consist of one or or more lines of data.
 *  A mixer will be set up with one single data line for audio data and additional lines (aka channels/tracks)
 *  may be added to the audio system.
 */


public class MixerSetUp {

    private AudioSystem audioSystem;
    private Mixer mixer;
    private Mixer.Info [] mixInfos;
    private ArrayList <Track> tracks;
    private Line [] lines;
    private OutputTrack output;
    private int trackCount;
    private long playOffset;

    /**
     * Create a mixer object with a given i/o choice from mixinfos. This may be changed in preferences as a later date.
     *
     **/

    public MixerSetUp(int audioPreferences) throws LineUnavailableException{

        mixInfos = AudioSystem.getMixerInfo();   // get the list of available i/o (built-in/soundcard etc).

        mixer = AudioSystem.getMixer(mixInfos[audioPreferences]);  // assign our mixer to a chosen i/o system.

        trackCount = 0;
        playOffset = 0;

        tracks = new ArrayList<>();
        System.out.println("tracks: " + tracks.size());


        System.out.println("\nTracks in project: " + trackCount());    // TEST TO SEE LINES IN MIXER CORRESPONDS TO ADD TRACK.

        //playOutput();

    }

    /**
     * Add a new track to the mixer. Specify parameter for type of track set-up.
     */

    public Track addTrack (String name, File file, float volume) {
        Track newTrack = null;
        try {
            newTrack = new Track(name, file, volume);
            tracks.add(newTrack);
        }
        catch (LineUnavailableException lue) {
            System.out.println(lue.getMessage());
        }
        trackCount ++;

        System.out.println("Tracks in output: " + tracks.size());

        lines = mixer.getSourceLines();  // refresh amount of lines in mixer.

        return newTrack;
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

            for (Track track: tracks) {
                output.addToOutput(track);
            }
            try {
                output.playTrack();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

    }

    public void pauseOutput (){
        playOffset = output.pause();

    }
    public void stopOutput (){
        playOffset = output.stop();
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
