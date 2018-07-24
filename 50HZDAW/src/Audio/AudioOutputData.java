package Audio;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class AudioOutputData {

    private byte [] outputByteArray;
    private float [] outputFloatArray;
    private ByteArrayInputStream inputPostStream;
    private ArrayList<Track> tracks;
    private ArrayList<float []> allTrackFloatData;
    private ByteToFloat byteToFloat;


    /**
     * Create an output channel that plays all tracks.
     * @param tracks The list of tracks and their data.
     */

    public AudioOutputData(ArrayList<Track> tracks) {

        this.tracks = tracks;
        allTrackFloatData = new ArrayList<>();
        byteToFloat = new ByteToFloat();
        createOutputData();

    }

    /**
     * Fill array list of float arrays to get all post-processed audio float data in one list. To be added to one float array (output).
     */

    public void createOutputData () {


        for(int i = 0; i < tracks.size(); i++) {
            allTrackFloatData.add(byteToFloat.byteToFloatArray(tracks.get(i).getTrackData(), byteToFloat.getMaxValue()));
        }
        System.out.println(allTrackFloatData.size());

    }

    /**
     * Combine all audio data into one array for output playback.
     */

    public void addDataForOutput() {

        for(int i = 0; i < allTrackFloatData.size(); i++) {
            outputFloatArray[i] = allTrackFloatData.get(i)[i];
        }

    }



    public void playOutput() {


    }
}
