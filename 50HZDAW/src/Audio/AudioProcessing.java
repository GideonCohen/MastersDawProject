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
    private byte [] stereoPostByteArray;
    private ByteToFloat byteToFloat;
    private float volume;

    public AudioProcessing (float [] stereoFloatArray,float volume) {

        this.stereoFloatArray = stereoFloatArray;
        byteToFloat = new ByteToFloat();
        this.volume = volume;

        adjustVolume();

    }



    /**
     * Adjust the volume of an audio float array by multiplying its values by a factor of x.
     */

    public void adjustVolume () {   // implement volume control param.


        for(int i = 0; i < stereoFloatArray.length; i++) {
            stereoFloatArray[i] = (stereoFloatArray [i] * volume);      // increase volume by factor of x.
        }

    }


    /**
     * Get stereo array of processed audio to be sent to audio track data. This will be the final product for playback.
     * @return
     */

    public float [] getProcessedAudio () {

        return stereoFloatArray;
    }


}
