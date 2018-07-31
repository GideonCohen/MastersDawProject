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
    private boolean pause;
    private long trackOffset;
    private int count;






    /**
     * Create an output channel that plays all tracks.
     *
     */

    public OutputTrack(String name, long offset) throws LineUnavailableException {

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        trackName = name;
        newLine = new DataLine.Info(SourceDataLine.class, audioFormat);
        // set up a data line with given audio format and specify the type of line it will be (source data line).
        source = (SourceDataLine) AudioSystem.getLine(newLine);
        source.open(audioFormat, source.getBufferSize()); //

        bufferSize = 1024;
        readBufferSize = bufferSize;
        sdlBufferSize = bufferSize * 2;

        minValue = -32768;
        maxValue =  32767;

        tracks = new ArrayList<>();
        byteToFloat = new ByteToFloat();

        readBuffer = new byte [readBufferSize];

        pause = false;
        trackOffset = offset;
        count = 1;

    }




    /**
     * Add track to output.
     */

    public void addToOutput(Track track) {

        tracks.add(track);
        outputFloatArray = new float [tracks.get(0).getTrackOutput().length];
        addDataForOutput1();


    }

    /**
      * Add all data from tracks and normalize output. This will be the result from adding all PCM values.
      */

    public float [] addDataForOutput(float [] normalizedOutput) {


        for(int i = 0; i < tracks.size(); i++)

            currentFloatArray = byteToFloat.byteToFloatArray(tracks.get(i).getTrackData(), tracks.get(i).getTrackDataObject().getMaxValue());

            for(int j = 0; j < readBufferSize; j++) {

                normalizedOutput[j] += currentFloatArray[j];
            }

        for(int j = 0; j < readBufferSize; j++) {
                if(normalizedOutput[j] > 1) {
                    normalizedOutput[j] = 1;
                }
                if(normalizedOutput[j] < -1) {
                    normalizedOutput[j] = -1;
                }
        }
        return normalizedOutput;

        }

    /**
     * Add all data from tracks and normalize output. This will be the result from adding all PCM values.
     */

    public void addDataForOutput1() {

        for (int i = 0; i < tracks.size(); i++) {
            currentFloatArray = tracks.get(i).getTrackOutput();
            for (int j = 0; j < currentFloatArray.length; j++) {
                try {
                    outputFloatArray [j] += currentFloatArray [j];
                } catch (IndexOutOfBoundsException e) {
                    //System.out.println("System out of bounds exception at : " + i);
                }
            }
        }
        outputBytes = byteToFloat.floatToByteArray(outputFloatArray, minValue, maxValue);
        outputStream = new ByteArrayInputStream(outputBytes);

    }


    /**
     * Play track. // plays back post-processed audio data.
     */

    public void playTrack () throws IOException {


        source.start();

        int numBytesRead = 0;

        try {
            System.out.println("Resume at : " + trackOffset);
            outputStream.skip(trackOffset);
            System.out.println("Offset is " + trackOffset);
            while (((numBytesRead = outputStream.read(readBuffer)) != -1) && pause == false){

                source.write(readBuffer, 0, numBytesRead);
                //System.out.println(count);
                count++;
                //System.out.println(numBytesRead);

            }
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
        }

        if (numBytesRead == -1) {
            count = 0;
        }
        source.stop();
        source.drain();
        outputStream.reset();

        //   }
        //  };

    }


    /**
     * Play track. // plays back post-processed audio data.
     */

    public void play2() throws IOException {



        source.start();

        //Thread playbackThread = new Thread () {

        //   public void run() {

        outputBytes = new byte [sdlBufferSize];
        int numBytesRead = 0;
        try {
            while (numBytesRead < 10) {

                readFloatBuffer = new float [readBufferSize];
                readFloatBuffer = addDataForOutput(readFloatBuffer);
                outputBytes = byteToFloat.floatToByteArray(readFloatBuffer, minValue, maxValue);
                source.write(outputBytes, 0, sdlBufferSize);

                numBytesRead ++;

            }
         } catch (IllegalArgumentException iae) {
           // System.out.println(iae.getMessage());
         }
        source.stop();
        source.drain();


        //  };

    }

    public long pause() {

        pause = true;
        trackOffset = count * readBuffer.length;
        System.out.println("Paused at " + trackOffset);
        System.out.println("Pause pressed. playback interrupted");
        System.out.println("Interrupt");
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
}
