package Audio;


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Track/channel class represents a given line of data within a mixer. A line then needs to be filled with some audio
 * data which may be from recorded input or .wav samples.
 * Track may be a superclass of other tracks that can be created. We may have audio data (recorded and sample based) as
 * well as MIDI data for a track.
 */


public class Track {

    private AudioFormat audioFormat;
    private String trackName;
    private int trackNumber;
    private DataLine.Info newLine;
    private SourceDataLine source;
    private byte [] playbackBuffer;
    private byte [] processedByteArray;
    private ArrayList<AudioData> audioData;
    private ArrayList<File> files;
    private AudioProcessing audioProcessing;
    private ByteToFloat byteToFloat;
    private StereoSplit stereoSplit;
    private int minValue;
    private int maxValue;

    private int trackSizeSeconds;
    private float [] trackBuffer;   // fill track buffer with 0's for silence.
    private long dataFinish;

    private boolean solo;
    private boolean mute;







    /**
     * Create a data line with specific audio settings that may be changed by the user. One must specify the format
     * of the audio file prior to creating a line. Working with .wav formats only. (PCM-Signed, little endian).
     */
;
    public Track (String name, float volume) throws LineUnavailableException {

        solo = false;
        mute = false;

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            // default audio format for data line if no file is added. will change once audio file is added.
        trackName = name;

        newLine = new DataLine.Info(SourceDataLine.class, audioFormat);
        // set up a data line with given audio format and specify the type of line it will be (source data line).
        source = (SourceDataLine) AudioSystem.getLine(newLine);
        source.open(audioFormat, source.getBufferSize()); // line must be open to appear in mixer
        // still data to be written before any playback!!!  also need to call start before write.
        audioData = new ArrayList<>();
        files = new ArrayList<>();

        /*
        String filepath_24bit = "50HZDAW/Samples/emotionalpads.wav";
        String hello16bit = "50HZDAW/Samples/Hello16bit.wav";
        String filepath_16bit = "50HZDAW/Samples/doublebass.wav";
        String filepath1_24bit = "50HZDAW/Samples/loopy3.wav";

        File file = new File(filepath_16bit);
        File file1 = new File(filepath_24bit);
        File file2 = new File(filepath1_24bit);
        File file3 = new File(hello16bit);


        addAudioTrackData(file2, 4000);    // add an audio file to be part of the tracks stream. audio processing can be added to a track.
        addAudioTrackData(file2, 2000);    // add an audio file to be part of the tracks stream. audio processing can be added to a track.
        addAudioTrackData(file1, 10181);     /// audio files cannot overlap! they must be be added at different positions in byte array.
        addAudioTrackData(file, 30000);
        moveAudioFile(1, 13000);
        */

        playbackBuffer = new byte [source.getBufferSize()];

        byteToFloat = new ByteToFloat();


        addDataToTrack();

       // moveAudioFile(0, 2000);

        audioProcessing = new AudioProcessing();


    }


    public Track () {

    }


    /**
     * Set the name of a given track.
     * @param name the name to set the track to.
     */

    public void setTrackName (String name) {

       trackName = name;

    }


    /**
     * Move an audio files position within a track.
     */

    public void moveAudioFile(int index, long startPosition) {


        audioData.get(index).setStart(startPosition);
        audioData.get(index).setFinish();
        addDataToTrack();
    }



    /**
     * Obtain the name of a given track.
     * @return the name of a given track.
     */

    public String getName () {

        return trackName;
    }

    /**
     * Set track number of a newly created track. Adjust numbers if tracks are removed.
     * @param track The number to set the track number to.
     */

    public void setTrackNumber(int track) {

        trackNumber = track;
    }

    /**
     * Obtain a given tracks number.
     * @return The track number to be returned.
     */

    public int getTrackNumber() {

        return trackNumber;
    }


    /**
     * Return the audio format of a given line. Standardised 16-bit PCM audio format is set for an empty line or audioFormat
     * is determined depending on file added.
     * @return audioFormat The audio format of a line.
     *
     */

    public AudioFormat getFormat () {

        return audioFormat;

    }


    /**
     * Add an audio file to the data line which should change its format.
     * @ param file: the file to be added
     * @ param start: place data in track at certain millisecond position.
     */

    public void addAudioTrackData (File file, long start) throws LineUnavailableException {

        try {

            AudioData audio = new AudioData(file, start);
            //if(checkForOverLap(audio) == true) {
            audioData.add(audio);
            files.add(file);
           // }
            //else {
           //     System.out.println("AUDIO FILE OVERLAP: PLACE FILE IN ANOTHER POSITION");     // CHECK TO SEE IF AUDIO FILES OVERLAP - USER SHOULD AVOID!
            //}
            audioFormat = audioData.get(0).getFileFormat();    // set format of track once audio file has been added (may be some restrictions).

            minValue = - ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1));    // calculate min & max representable int value for n-bit number.
            maxValue = ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1)) - 1;   // min & max values can be passed when converting byte to float & vice versa.


        }
        catch (UnsupportedAudioFileException uafe) {
            System.out.println(uafe.getMessage());
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }

        source.close();  // close line at previous audio format and open again once audio file is added.
        source.open(audioFormat, source.getBufferSize()); // line must be open to appear in mixer
        addDataToTrack();

    }

    /**
     * Check to see whether an audio file will overlap or clash with another block of audio. We don't want this to happen.
     */

    public boolean checkForOverLap(AudioData audio) {


        for(AudioData data: audioData) {
            if(audio.getStart() >= data.getStart() && audio.getStart() <= data.getFinish()) {
                return false;
            }
            else if(audio.getFinish() >= data.getStart() && audio.getFinish() <= data.getFinish()) {
                return false;
            }
        }
        return true;
    }



    /**
     * Add all audio files to the track data. Consider start position of where audio file is placed on the track.
     */


    public void addDataToTrack () {

        dataFinish = 0;
        long dataOffset = 0;

        for(int i = 0; i < audioData.size(); i++) {
            if(audioData.get(i).getFinish() > dataFinish) {
                dataFinish = audioData.get(i).getFinish();
            }
        }

        trackBuffer = new float [(int)dataFinish];    // set buffer size to finish of audio content. // represents float buffer for 24-bit audio.

        for(int i = 0; i < audioData.size(); i++) {
            dataOffset = audioData.get(i).getStart();
            float [] currentFloatArray = audioData.get(i).getStereoFloatArray();
            for (int j = 0; j < currentFloatArray.length; j++) {
                trackBuffer[(int) dataOffset] = currentFloatArray [j];
                dataOffset++;
            }

        }
    }


    /**
     * Add audio processing to a track.
     */

    public void addVolume (float volume) {

        audioProcessing.setVolume(volume, trackBuffer);
        trackBuffer = audioProcessing.getProcessedAudio();    // get audio after all processing is done
    }

    /**
     * Add audio processing to a track.
     */
    public void setPan (float pan) {

        audioProcessing.setVolume(pan, trackBuffer);
        trackBuffer = audioProcessing.getProcessedAudio();    // get audio after all processing is done
    }


    /**
     *  Add delay to audio.
     */

    public void setDelay () {

        audioProcessing.setDelay(trackBuffer, 250, 3, 50f);
        trackBuffer = audioProcessing.getProcessedAudio();

    }





    /**
     * Get audio data in form of byte array from a given track/channel.
     */

    public AudioData getTrackDataObject () {

        return audioData.get(0);
    }


    /**
     * Get float array of processed audio data.
     */

    public float [] getTrackData () {

        return trackBuffer;
    }




    /**
     * Set track to solo. Only the data from this track only will be added to output.
     */

    public void setSolo () {

        if(solo == false) {
            solo = true;
        }
        else {
            solo = false;
        }
    }


    /**
     * Set track to mute. Data from this track will not be added to the output.
     */

    public void setMute () {

        if(mute == false) {
            mute = true;
        }
        else {
            mute = false;
        }
    }


    /**
     * Find out whether a track is set to solo or not.
     */

    public boolean getSolo () {

        return solo;
    }


    /**
     * Find out whether a track is set to mute or not.
     */

    public boolean getMute () {

        return mute;
    }


    /**
     *  Get length of a track in number of float values.
     */

    public long trackLength () {

        return dataFinish;
    }



}
