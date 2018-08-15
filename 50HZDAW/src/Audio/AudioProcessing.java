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
    private StereoSplit stereoSplit;

    public AudioProcessing () {

        byteToFloat = new ByteToFloat();
        stereoSplit = new StereoSplit();


    }

    /**
     * Set volume.
     */

    public void setVolume (float newVolume, float [] trackBuffer) {

        this.stereoFloatArray = trackBuffer;
        this.volume = newVolume;
        adjustVolume();
    }


    /**
     * Set volume.
     */

    public void setPan (float leftGain, float rightGain,  float [] trackBuffer) {

        stereoSplit.split(stereoFloatArray);
        stereoSplit.convergeMonoArrays(leftGain, rightGain);

    }

    /**
     * Set delay.
     */

    public float [] setDelay (float [] test, int delay, int feedBack, float fadeOut) {

        System.out.println("PRE DELAY ARRAY LENGTH: " + test.length);

        fadeOut = fadeOut * 0.01f; // method will be fed percentage values. This will turn 80 into 0.8 etc.

            int delayFixedValue = delay;
            float[] newTest = new float[test.length + (delay * feedBack)];
            for (int i = 0; i < feedBack; i++) {
                newTest = addOneDelay(test, delay);
                test = newTest;
                if (i >= 1) {
                    for (int j = 0; j < test.length; j++) { //fadeout layers after the first one has been.
                        test[j] = test[j] * fadeOut;
                    }
                }
                delay = delayFixedValue + delay;
            }

            System.out.println("POST DELAY ARRAY LENGTH: " + newTest.length);
            this.stereoFloatArray = newTest;

            return newTest;

    }


    /**
     * Single delay.
     * @param test
     * @param delay
     * @return
     */

    private float[] addOneDelay(float[] test, int delay) {

        delay = (88200 / 1000) * delay; //finds index for stereoTrack


        float[] newTest = new float[test.length + delay];

        //adds the un-layered beginning to the delayed signal.
        for (int i = 0; i < delay; i++) {
            newTest[i] = test[i];
        }

        int count = 0;
        // layers the middle part.
        for (int i = delay; i < test.length; i++) {
            newTest[i] = test[count] + test[count + delay];
            count++;
        }
        //adds the un-layered tail to the end.
        int endIndex = test.length - delay;
        for (int i = endIndex; i < test.length; i++) {
            newTest[i + delay] = test[i];
        }
        return newTest;
    }



    public float[] delayLoop(float[] test, int delay, int layers, float fadeOut) {

        float[] newTest = new float[test.length + (delay * layers)];    // delay amount must be greater than sample length.
        int delayFixedValue = delay;

        float[] layer = new float[test.length];
        for (int i = 0; i< test.length; i++ ) {
            layer[i] = test[i];
        }

        for (int i = 0; i < layers; i++) {
            newTest = add(test, layer, delay,fadeOut);
            test = newTest;
            delay = delayFixedValue + delay;
            fadeOut = 1 * fadeOut;
        }
        return newTest;
    }


    public float[] add(float[] originalFloats, float [] layer, int delay, float fadeOut) { ;

        int difference = originalFloats.length - layer.length;
        float[] newFloats = new float[(originalFloats.length - difference) + delay];

        delay = (88200/1000) * delay;

        for (int i=0; i< layer.length; i++) {
            layer[i] =+ (layer [i] * fadeOut);
        }

        int count = 0;
        for (int i = 0; i < newFloats.length; i++) {
            if (i < delay) {
                newFloats[i] = originalFloats[i];
            } else if (i >= delay && i < originalFloats.length) {
                newFloats[i] = (originalFloats[i] + layer[count]);
                count++;
            } else if (i >= originalFloats.length && count < layer.length) {
                newFloats[i] = layer[count];
                count++;
            }
        }
        return newFloats;
    }



    /**
     * Adjust the volume of an audio float array by multiplying its values by a factor of x.
     */

    public void adjustVolume () {   // implement volume control param.

        for(int i = 0; i < stereoFloatArray.length; i++) {
            stereoFloatArray[i] = (stereoFloatArray [i] * volume);      // increase volume by factor of x.
        }

    }

    public float getVolume () {
        System.out.println(volume);
        return volume;
    }


    /**
     * Get stereo array of processed audio to be sent to audio track data. This will be the final product for playback.
     * @return
     */

    public float [] getProcessedAudio () {

        return stereoFloatArray;
    }


}
