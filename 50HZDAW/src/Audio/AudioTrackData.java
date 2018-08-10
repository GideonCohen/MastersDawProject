package Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;


public class AudioTrackData {

    private AudioInputStream inputStream;
    private AudioFormat audioFormat;
    private long audioFileLength;
    private int frameSize;
    private float frameRate;
    private float durationInSeconds;
    private int minValue;
    private int maxValue;
    private byte [] stereoByteArray;
    private ByteToFloat byteToFloat;
    private boolean reverse;

    private long start;
    private long finish;



    public AudioTrackData (File file, long start) throws UnsupportedAudioFileException, IOException {

        this.start = setStart(start);
        byteToFloat = new ByteToFloat();
        setUpStream(file);
        setStereoByteArray();
        reverse = false;

    }

    /**
     * Set up an audio stream from the file given.
     */

    public void setUpStream(File file) throws UnsupportedAudioFileException, IOException {

        inputStream = AudioSystem.getAudioInputStream(file);
        audioFormat = inputStream.getFormat();
        audioFileLength = file.length();
        frameSize = audioFormat.getFrameSize();
        frameRate = audioFormat.getFrameRate();
        durationInSeconds = (audioFileLength / (frameSize * frameRate));
        finish = audioFileLength + start;

        minValue = - ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1));    // calculate min & max representable int value for n-bit number.
        maxValue = ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1)) - 1;

        System.out.println("Audio Format of File " + audioFileLength + " bytes " + (float)audioFileLength/1000000 + " mb" +  "  " + audioFormat + "  " + frameSize + "  "  + frameRate + "  "  + durationInSeconds);

        System.out.println("Representation limits for " + audioFormat.getSampleSizeInBits() + "-bit integer: " + minValue + " to " + maxValue + "\n");


    }

    /**
     * Fill byte array with data from audio input stream. Split audio file into two mono left and right streams.
     */

    public void setStereoByteArray () throws IOException {

        stereoByteArray = new byte [(int)audioFileLength];
        inputStream.read(stereoByteArray);  // sets a stereo byte array to bit split for processing.    // fill byte array with pre-processed audio.

       // reverse = true; // set audio file in reverse.
      //  reverseAudio();


    }

    /**
     * Reverse audio file. Flip negative values to positive and positive values to negative.
     */


    public void reverseAudio () {

        if(reverse == true) {

            for(int i = 0; i < stereoByteArray.length; i++) {

                    stereoByteArray[i] = ((byte)(stereoByteArray[i] * -1));
            }
        }


    }


    /**
     *
     * Get minimum representable value.
     */

    public int getMinValue () {

        return minValue;
    }

    /**
     *
     * Get minimum representable value.
     */

    public int getMaxValue () {

        return maxValue;
    }


    /**
     *
     * Get format of audio file/audio input stream to be passed to Track.
     */

    public AudioFormat getFileFormat () {

        return audioFormat;
    }


    /**
     * Get byte array of processed audio. This can be used for addition with other audio data for full output playback.
     */

    public byte [] getStereoByteArray () {

        return stereoByteArray;
    }


    /**
     * Get byte array of processed audio. This can be used for addition with other audio data for full output playback.
     */

    public float [] getStereoFloatArray () {

        return byteToFloat.byteToFloatArray(stereoByteArray, maxValue);
    }



    /**
     * Set start time of audio file in track stream.
     */

    public long setStart(long start) {

        long startByte = (start/1000) * 176400;
        return startByte;
    }



    /**
     * Set finish time of audio file in track stream.
     */

    public void setFinish(long finish) {

        this.finish = finish;

    }

    /**
     * Get start time of audio file in track stream.
     */

    public long getStart() {

        return start;

    }

    /**
     * Get finish time of audio file in track stream.
     */

    public long getFinish() {

        return finish;

    }








}



