package Audio;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class AudioData {

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
    private int bitDepth;
    private long bytesPerSecond;
    private long floatsPerSecond;
    private InputStream is;
    private byte [] mp3TempArray;



    public AudioData (File file, long startMillis) throws UnsupportedAudioFileException, IOException {

        this.start = startMillis;
        byteToFloat = new ByteToFloat();
        setUpStream(file);
        setStereoByteArray();
        reverse = false;

    }

    /**
     * Set up an audio stream from the file given.
     */

    public void setUpStream(File file) throws UnsupportedAudioFileException, IOException {

        //
        // MP3AudioDecoder mp3Decoder = new MP3AudioDecoder();
        //inputStream = mp3Decoder.decode(file);               // ONLY DECODE IF MP3 IS DETECTED AND CAN'T ASSIGN NORMAL INPUT STREAM.

        inputStream = AudioSystem.getAudioInputStream(file);

       // mp3TempArray = new byte [(int)file.length()];



        //  is = new FileInputStream(file);
       // is.read(mp3TempArray);           // set a basic wav file format for an mp3 file.
       // audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
      //  inputStream = AudioSystem.getAudioInputStream(is);
       // inputStream = new AudioInputStream(is, audioFormat, file.length());
       // inputStream = AudioSystem.getAudioInputStream(file);

        audioFormat = inputStream.getFormat();


        audioFileLength = file.length();
        frameSize = audioFormat.getFrameSize();
        frameRate = audioFormat.getFrameRate();
        durationInSeconds = (audioFileLength / (frameSize * frameRate));
        bitDepth = audioFormat.getSampleSizeInBits();
        bytesPerSecond = (long)(frameRate * frameSize);
        floatsPerSecond = (long)(2 * frameRate);
        // add file in this position
        setStart(start);
        minValue = - ((int)Math.pow(2, bitDepth-1));    // calculate min & max representable int value for n-bit number.
        maxValue = ((int)Math.pow(2, bitDepth-1)) - 1;

        System.out.println("Audio Format of File " + audioFileLength + " bytes " + (float)audioFileLength/1000000 + " mb" +  "  " + audioFormat + "  " + frameSize + "  "  + frameRate + "  "  + durationInSeconds);


        // System.out.println("Representation limits for " + audioFormat.getSampleSizeInBits() + "-bit integer: " + minValue + " to " + maxValue + "\n");


    }

    /**
     * Fill byte array with data from audio input stream. Split audio file into two mono left and right streams.
     */

    public void setStereoByteArray () throws IOException {

        stereoByteArray = new byte [(int)audioFileLength];
        inputStream.read(stereoByteArray);  // sets a stereo byte array to bit split for processing.    // fill byte array with pre-processed audio.
        setFinish();
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
     * Set start time of audio file in track stream.    // 88200 float values per second for 16bit & 24bit!
     */

    public long setStart(long startMillis) {

        long startFloat = (startMillis * floatsPerSecond)/1000;
        this.start = startFloat;
        return startFloat;
    }



    /**
     * Set finish time of audio file in track stream.
     */

    public long setFinish() {

        long finishFloat = this.start + (getStereoFloatArray().length);
        this.finish = finishFloat;
        System.out.println("Finish Byte: " + finish);
        return finishFloat;


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



