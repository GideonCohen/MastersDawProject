package Audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Output Track inherits attributes from Track such as set up of Data Line and playback. Output class is used for playback
 * of multiple Tracks(super).
 */


public class OutputTrack {

    private float [] outputFloatArray;
    private float [] currentFloatArray;
    private byte [] readBuffer;
    private float [] readFloatBuffer;
    private byte [] outputBytes;
    private ArrayList<Track> tracks;
    private ByteToFloat byteToFloat;
    private int bufferSize;
    private int readBufferSize;
    private int sdlBufferSize;
    private AudioFormat audioFormat;
    private String trackName;
    private DataLine.Info newLine;
    private SourceDataLine source;
    private int minValue;
    private int maxValue;
    private ByteArrayInputStream outputStream;
    private Timing timing;
    private float bpm;

    private long outputLength;

    private BPMConverter bpmConverter;

    private boolean pause;
    private long trackOffset;
    private int count;






    /**
     * Create an output channel that plays all tracks.
     *
     */

    public OutputTrack(String name, long offset) throws LineUnavailableException {

        bpm = 100;
        bpmConverter = new BPMConverter();
        bpmConverter.setBPM((int)bpm);

        System.out.println(bpmConverter.setBars(4));
        System.out.println(bpmConverter.setQuarterBeat(16));
        System.out.println(bpmConverter.setEighthBeat(32));


        // OUTPUT TRACK IS AT 24-BIT STEREO.
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 24, 2, 6, 44100, false);
        trackName = name;
        newLine = new DataLine.Info(SourceDataLine.class, audioFormat);
        // set up a data line with given audio format and specify the type of line it will be (source data line).
        source = (SourceDataLine) AudioSystem.getLine(newLine);
        source.open(audioFormat, source.getBufferSize()); //

        bufferSize = 1024*6;
        readBufferSize = bufferSize * 2;
        sdlBufferSize = bufferSize * 4;

        minValue = - ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1));    // calculate min & max representable int value for n-bit number.
        maxValue = ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1)) - 1;

        tracks = new ArrayList<>();
        byteToFloat = new ByteToFloat();
//       timing.getTimerMillis();

        trackOffset = offset;

        readBuffer = new byte [source.getBufferSize()];    // 1 seconds worth of audio every iteration. 88.2 bytes every ms

        // pause fix i think, was reinitialising count each time
        count = (int) trackOffset/readBuffer.length;
    }

    /**
     * Add track to output.
     */

    public void addToOutput(Track track) {

        tracks.add(track);
        addDataForOutput1();   // updates all data in track (needs to be called if any data from track has been changed)

    }

    /**
      * Add all data from tracks and normalize output. This will be the result from adding all PCM values.
      */

    public float [] addDataForOutput(float [] normalizedOutput) {


        for (int i = 0; i < tracks.size(); i++)     // NOT IN USE

            currentFloatArray = tracks.get(i).getTrackData();

        for (int j = 0; j < readBufferSize; j++) {

            normalizedOutput[j] += currentFloatArray[j];
        }

        for (int j = 0; j < readBufferSize; j++) {
            if (normalizedOutput[j] > 1) {
                normalizedOutput[j] = 1;
            }
            if (normalizedOutput[j] < -1) {
                normalizedOutput[j] = -1;
            }
        }
        return normalizedOutput;

    }

    /**
     *
     * @param track
     */
    public void removeTrack(Track track) {
        tracks.add(track);
        int indexOf = tracks.indexOf(track);
        currentFloatArray = tracks.get(indexOf).getTrackData();
        for (int j = 0; j < currentFloatArray.length; j++) {
            outputFloatArray[j] -= currentFloatArray[j];
        }
    }

    /**
     * Add all data from tracks and normalize output. This will be the result from adding all PCM values.
     * Every time processing or change to audio - this method can be used to refresh out output from the changes made.
     */

    public void addDataForOutput1() {

        outputLength = 0;
        for(Track track: tracks)
            if(track.trackLength() > outputLength) {
            outputLength = track.trackLength();
        }

        outputFloatArray = new float [(int)outputLength];  // output is length of longest track (or length of first track for now).

        boolean soloMode = false;

        for(int i = 0; i < tracks.size(); i++) {
            if(tracks.get(i).getSolo() == true) {
                soloMode = true;
            }
        }


        if(soloMode == true) {

            //SOLO PLAYBACK
            for (int i = 0; i < tracks.size(); i++) {
                if (tracks.get(i).getSolo() == true) {                    // only play tracks with solo on.
                    currentFloatArray = tracks.get(i).getTrackData();
                    for (int j = 0; j < currentFloatArray.length; j++) {
                        outputFloatArray[j] += currentFloatArray[j];
                    }
                }
                else {
                    currentFloatArray = new float[tracks.get(i).getTrackData().length];   // create an empty array of track size when on mute.
                }

            }
        }

        else {
            //NORMAL PLAYBACK
            for (int i = 0; i < tracks.size(); i++) {
                if (tracks.get(i).getMute() == false) {                    // don't play tracks on mute.
                    currentFloatArray = tracks.get(i).getTrackData();
                    for (int j = 0; j < currentFloatArray.length; j++) {
                        outputFloatArray[j] += currentFloatArray[j];
                    }
                } else {
                    currentFloatArray = new float[tracks.get(i).getTrackData().length];   // create an empty array of track size when on mute.
                }
            }
        }

        outputBytes = byteToFloat.floatToByteArray(outputFloatArray, minValue, maxValue);     // transform float array to 24-bit byte array
        System.out.println(outputBytes.length);                                              // (format specified in format of output line).
        outputStream = new ByteArrayInputStream(outputBytes);

    }


    /**
     * Play track. // plays back post-processed audio data.
     */

    public void playTrack () throws IOException {

        source.start();

       // timing = new Timing(0, 100000, true);
      //  timing.getTimerMillis(bpm);


        int numBytesRead = 0;

        try {

            System.out.println("Resume at : " + trackOffset);
            outputStream.skip(trackOffset);
            System.out.println("Offset is " + trackOffset);
            while ((numBytesRead = outputStream.read(readBuffer)) != -1 && pause == false) {    // 40 iterations of 88200. 88200 = 1 second of playback.
                source.write(readBuffer, 0, readBuffer.length);               //  4 bytes represent a stereo datapoint within a sample.
                // System.out.println(count);                                   // 44100 sample rate = each stereo sample is 44100 * 4 = 176400
                count++;                                                    // total bytes = length of audio file * bytespersecond (176400)
            }
            // catch last write before source is killed
            source.write(readBuffer, 0, 44100);
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());          //
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());          // SHOULD BE CAUGHT CLIENT SIDE...
        }

        System.out.println(count);

        if (numBytesRead == -1) {
            count = 0;
        }
        System.out.println("Done");
        source.stop();
        source.drain();
        outputStream.reset();

        }



    /**
     * Set mute.
     */

    public void setMute(int trackNumber) {

        tracks.get(trackNumber).setMute();
        addDataForOutput1();
    }

    /**
     * Set solo.
     */

    public void setSolo(int trackNumber) {

        tracks.get(trackNumber).setSolo();

        addDataForOutput1();

    }

    /**
     * Set mute.
     */

    public boolean getMute(int trackNumber) {

        return tracks.get(trackNumber).getMute();

    }

    /**
     * Set solo.
     */

    public boolean getSolo(int trackNumber) {

        return tracks.get(trackNumber).getSolo();

    }

    public long pause() {

        trackOffset = count * readBuffer.length;
        pause = true;
        /*
        System.out.println("Paused at " + trackOffset);
        System.out.println("Pause pressed. playback interrupted");
        System.out.println("Interrupt");
        */
        source.stop();
        source.drain();
        outputStream.reset();
        return trackOffset;
    }


    public long stop() {

        pause = true;
        count = 0;
        System.out.println("Track stopped start again from beginning.");
        source.stop();
        source.drain();
        outputStream.reset();
        return 0;
    }

    public boolean isPlaying(){
        return pause;
    }

    public byte[] addDataForOutput1Export() {

        outputLength = 0;
        for(Track track: tracks)
            if(track.trackLength() > outputLength) {
                outputLength = track.trackLength();
            }

        outputFloatArray = new float [(int)outputLength];  // output is length of longest track (or length of first track for now).

        boolean soloMode = false;

        for(int i = 0; i < tracks.size(); i++) {
            if(tracks.get(i).getSolo() == true) {
                soloMode = true;
            }
        }


        if(soloMode == true) {

            //SOLO PLAYBACK
            for (int i = 0; i < tracks.size(); i++) {
                if (tracks.get(i).getSolo() == true) {                    // only play tracks with solo on.
                    currentFloatArray = tracks.get(i).getTrackData();
                    for (int j = 0; j < currentFloatArray.length; j++) {
                        outputFloatArray[j] += currentFloatArray[j];
                    }
                }
                else {
                    currentFloatArray = new float[tracks.get(i).getTrackData().length];   // create an empty array of track size when on mute.
                }

            }
        }

        else {
            //NORMAL PLAYBACK
            for (int i = 0; i < tracks.size(); i++) {
                if (tracks.get(i).getMute() == false) {                    // don't play tracks on mute.
                    currentFloatArray = tracks.get(i).getTrackData();
                    for (int j = 0; j < currentFloatArray.length; j++) {
                        outputFloatArray[j] += currentFloatArray[j];
                    }
                } else {
                    currentFloatArray = new float[tracks.get(i).getTrackData().length];   // create an empty array of track size when on mute.
                }
            }
        }

        outputBytes = byteToFloat.floatToByteArray(outputFloatArray, minValue, maxValue);     // transform float array to 24-bit byte array

        return outputBytes;
    }

    public long getCurrentPosition() {
        return count * readBuffer.length;
    }
}
