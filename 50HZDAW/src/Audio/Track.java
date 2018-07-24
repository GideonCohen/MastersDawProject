package Audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Track/channel class represents a given line of data within a mixer. A line then needs to be filled with some audio
 * data which may be from recorded input or .wav samples.
 * Track may be a superclass of other tracks that can be created. We may have audio data (recorded and sample based) as
 * well as MIDI data for a track.
 */


public class Track {

    private ByteArrayInputStream inputPostStream;
    private AudioFormat audioFormat;
    private String trackName;
    private int trackNumber;
    private DataLine.Info newLine;
    private SourceDataLine source;
    private byte [] playbackBuffer;
    private byte [] processedByteArray;
    private ArrayList<AudioTrackData> audioTrackData;
    private AudioProcessing audioProcessing;
    private ByteToFloat byteToFloat;

    private int trackSizeSeconds;
    private byte [] trackBuffer;   // fill track buffer with 0's for silence.






    /**
     * Create a data line with specific audio settings that may be changed by the user. One must specify the format
     * of the audio file prior to creating a line. Working with .wav formats only. (PCM-Signed, little endian).
     */
;
    public Track (String name, File file, float volume) throws LineUnavailableException {

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            // default audio format for data line if no file is added. will change once audio file is added.
        trackName = name;
        newLine = new DataLine.Info(SourceDataLine.class, audioFormat);
        // set up a data line with given audio format and specify the type of line it will be (source data line).
        source = (SourceDataLine) AudioSystem.getLine(newLine);
        source.open(audioFormat, source.getBufferSize()); // line must be open to appear in mixer
        // still data to be written before any playback!!!  also need to call start before write.
        audioTrackData = new ArrayList<>();
        addAudioTrackData(file);    // add an audio file to be part of the tracks stream. audio processing can be added to a track.
        playbackBuffer = new byte [source.getBufferSize()];

        byteToFloat = new ByteToFloat();

        trackBuffer = new byte [source.getBufferSize() * 120];     // all tracks should have the same data length for addition in output.

        addDataToTrack();
        addProcessing(volume);


    }


    /**
     * Set the name of a given track.
     * @param name the name to set the track to.
     */

    public void setTrackName (String name) {

       trackName = name;

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
     */

    public void addAudioTrackData (File file) throws LineUnavailableException {

        try {
            audioTrackData.add(new AudioTrackData(file));
            audioFormat = audioTrackData.get(0).getFileFormat();
        }
        catch (UnsupportedAudioFileException uafe) {
            System.out.println(uafe.getMessage());
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        source.close();  // close line at previous audio format and open again once audio file is added.
        source.open(audioFormat, source.getBufferSize()); // line must be open to appear in mixer

    }

    /**
     * Add all audio files to the track data.
     */

    public void addDataToTrack () {
        int check = 0;
        for(int i = 0; i < audioTrackData.get(0).getStereoByteArray().length; i++) {
            try {
                trackBuffer[i] = audioTrackData.get(0).getStereoByteArray()[i];
            } catch (IndexOutOfBoundsException e) {
                check++;
            }
        }
        System.out.println("Index problems: " + check + ".");
    }

    /**
     * Add audio processing to a track.
     *
     */

    public void addProcessing (float volume) {

        audioProcessing = new AudioProcessing(trackBuffer, audioTrackData.get(0).getMinValue(), audioTrackData.get(0).getMaxValue(), volume);
        trackBuffer = audioProcessing.getProcessedByteAudio();
        inputPostStream = new ByteArrayInputStream(trackBuffer);



    }

    /**
     * Play track. // plays back post-processed audio data.
     */

    public void playTrack () throws IOException {

        source.start();

        //Thread playbackThread = new Thread () {

        //   public void run() {

        int numBytesRead = 0;
        int count = 1;
        try {
            while ((numBytesRead = inputPostStream.read(playbackBuffer)) != -1) {
                    source.write(playbackBuffer, 0, numBytesRead);
                    //System.out.println(count);
                    count ++;

            }
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
        }

        source.stop();
        source.drain();
        inputPostStream.reset();

        //   }
        //  };

    }

    /**
     * Get audio data in form of byte array from a given track/channel.
     */

    public AudioTrackData getTrackDataObject () {

        return audioTrackData.get(0);

    }


    /**
     * Get audio data in form of byte array from a given track/channel.
     */

    public byte [] getTrackData () {

        return trackBuffer;   // track has an list of data added to track ( post processed )
        // for now we will use one piece of data to try and play tracks concurrently.
    }

    /**
     * Get float array of processed audio data.
     */

    public float [] getTrackOutput () {

        return byteToFloat.byteToFloatArray(trackBuffer, audioTrackData.get(0).getMaxValue());
    }



}
