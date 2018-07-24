package Audio;

/**
 * Any audio to be processed will be done in this intermediate class.
 * We can take a byte array of pre-processed audio, process it and then send to an array which
 * represents post-processed audio. From here we can write this effected audio for playback.
 * There will be separate classes for panning/volume control/EQ which will be processed and sent back to this
 * class which can then be sent to our class for playback.
 */

public class AudioProcessing {

    private float [] stereoFloatArray;
    private byte [] stereoPreByteArray;
    private byte [] stereoPostByteArray;
    private int minValue;   // min integer value for audio file.
    private int maxValue;   // max integer value for audio file.
    private ByteToFloat byteToFloat;
    private float volume;

    public AudioProcessing (byte [] stereoPreByteArray, int minValue, int maxValue, float volume) {

        this.stereoPreByteArray = stereoPreByteArray;
        this.minValue = minValue;
        this.maxValue = maxValue;
        byteToFloat = new ByteToFloat();
        this.volume = volume;

        adjustVolume();


    }



    /**
     * Adjust the volume of an audio float array by multiplying its values by a factor of x.
     */

    public void adjustVolume () {   // implement volume control param.

        stereoFloatArray = byteToFloat.byteToFloatArray(stereoPreByteArray, maxValue);

        System.out.println("Pre Volume : " + stereoFloatArray[100]);


        for(int i = 0; i < stereoFloatArray.length; i++) {
            stereoFloatArray[i] = (stereoFloatArray [i] * volume);      // increase volume by factor of x.
        }

        System.out.println("Post Volume : " + stereoFloatArray[100]);

        stereoPostByteArray = byteToFloat.floatToByteArray(stereoFloatArray, minValue, maxValue);

    }



    /**
     * Get stereo array of processed audio to be sent to audio track data. This will be the final product for playback.
     * @return
     */

    public byte [] getProcessedByteAudio () {

        return stereoPostByteArray;
    }

    /**
     * Get stereo array of processed audio to be sent to audio track data. This will be the final product for playback.
     * @return
     */

    public float [] getProcessedAudio () {

        return stereoFloatArray;
    }


}
