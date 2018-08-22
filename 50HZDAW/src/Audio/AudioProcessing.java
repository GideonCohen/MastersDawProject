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
     * Takes an existing array of floats and multiplies their values by a given amount.
     * This will increase the volume of the audio.
     * @param newVolume The amount to multiply.
     * @param trackBuffer The array of values to be multiplied by.
     */
    public void setVolume (float newVolume, float [] trackBuffer) {

        this.stereoFloatArray = trackBuffer;
        this.volume = newVolume;
        adjustVolume();
    }


    /**
     * Takes an array of floats and multiplies their values by a given amount. The first index of the array
     * will be multiplied by the left gain and the second will be multiplied by the right gain. These values
     * will be multiplied in turn.
     * @param leftGain Multiply the left channel by this value.
     * @param rightGain Multiply the right channel with this value.
     * @param trackBuffer The array of values to be multiplied.
     */
    public void setPan (float leftGain, float rightGain,  float [] trackBuffer) {

        stereoSplit.split(stereoFloatArray);
        stereoSplit.convergeMonoArrays(leftGain, rightGain);

    }

    /**
     * Will take an array of float values and will return that array reversed.
     * @param originalAudio The original array of data.
     * @return The reversed array of data.
     */
    public float [] reverseAudio (float [] originalAudio) {

        float [] reversedAudio = new float [originalAudio.length];

        for(int i = 0; i < originalAudio.length; i++) {

            int reverseIndex = (originalAudio.length - 1) - i;
            reversedAudio [i] = originalAudio[reverseIndex];

        }
        return reversedAudio;
    }


    /**
     * Takes a signal and adds a delay effect by layering it over itself.
     * The user can choose to reverse the layers going on top to create a reverse delay effect.
     * @param test the unprocessed signal
     * @param delay - how long to wait before each layer is added on
     * @param layers - how many layers to add on
     * @param fadeOut - how much the volume of each layer should reduce each time.
     * @param isReversed - whether the layers are reversed or not
     * @return the processed signal
     */
    public float[] delayLoop(float[] test, int delay, int layers, float fadeOut, int isReversed) {

        float[] newTest = new float[test.length + (delay * layers)];
        int delayFixedValue = delay;

        float[] layer = new float[test.length];

        for (int i = 0; i< test.length; i++ ) {
            layer[i] = test[i];
        }
        if(isReversed == 1) {
            layer = reverseAudio(layer);
        }
        for (int i = 0; i < layers; i++) {
            newTest = addOneDelay(test, layer, delay,fadeOut);
            test = newTest;
            delay = delayFixedValue + delay;
            fadeOut = 1 * fadeOut;
        }
        return newTest;
    }

    /**
     * Takes two float arrays. After a given delay the layer will be added to the original floats index by index.
     * The method will iterate the layer and multiply it by a fadeout value causing a percentage reduction in the amplitude values
     * in comparison to the original signal.
     * @param originalFloats - the original signal
     * @param layer - the signal to be added after a certain delay
     * @param delay - how long to wait before the layer is added
     * @param fadeOut - how much to reduce the signal by
     * @return - The processed signal
     */
    private float[] addOneDelay(float[] originalFloats, float [] layer, int delay, float fadeOut) {

        delay = 88200 / 1000 * delay;
        int difference = originalFloats.length - layer.length;
        float[] newFloats = new float[(originalFloats.length - difference) + delay];

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
     * Hard clips a digital signal by setting any amplitude values over a given threshold as equal to that threshold.
     * The result will be a waveform where the tops of the signal are flattened and the signal will sound distorted at
     * playback. The average loss in gain is restored to prevent any general loss in amplitude caused by processing.
     * @param test - the signal to distort
     * @param threshold - the threshold to flatten the signal at
     * @return the processed signal
     */
    public float[] distortion(float[] test, float threshold) {

        float initialAverageVolume = averageGain(test);
        for (int i = 0; i < test.length; i++) {
            if (test[i] > threshold) {
                test[i] = threshold;
            }
            if (test[i] < -threshold) {
                test[i] = -threshold;
            }
        }

        float processedAverageVolume = averageGain(test);

        float amplitudeRegain = initialAverageVolume/processedAverageVolume;


        for (int i = 0; i < test.length; i++) {
            test[i] *= amplitudeRegain;
        }
        return test;
    }


    /**
     * Returns a float value for the average gain of a handed signal
     * @param floats The array of floats to obtain an average gain from.
     * @return - average gain The average gain from the array of floats.
     */
    private float averageGain(float[] floats) {
        float sum = 0f;
        for (int i = 0; i < floats.length; i++) {
            if (floats[i] < 0) {
                sum += (-1 * floats[i]);
            }
            if (floats[i] > 0) {
                sum += floats[i];
            }
        }
        float averageGain = sum / floats.length;
        return averageGain;
    }



    /**
     * Takes a signal and iterates over both it's channels to filter out noise under a given threshold.
     * @param signal - the signal to be processed
     * @param threshold - the threshold to start filtering
     * @param attack - how quickly to turn down gain
     * @param release - how quickly to turn up gain when signal goes back over threshold.
     * @return - the processed signal
     */
    public float[] noiseGateTwoChannel(float[] signal, float threshold, int attack, int release) {
        signal = noiseGateOneChannel(signal,threshold,attack,release,0,signal.length-1); // does left channel
        signal = noiseGateOneChannel(signal,threshold,attack,release,1,signal.length); // does right channel.
        return signal;
    }

    /**
     * Iterates over one channel of a stereo signal and filters out noise under a given threshold.
     * When the volume goes under a threshold it is turned down gradually over the attack period.
     * When the method is holding the signal is set to silent.
     * If the signal goes back over the threshold it is gradually turned up again over the release period.
     * The start index and end index allow the noiseGateTwoChannel method to call this method and make it
     * iterate over both channels.
     * @param signal
     * @param threshold
     * @param attack
     * @param release
     * @param startIndex
     * @param endIndex
     * @return - the processed signal
     */
    public float[] noiseGateOneChannel(float[] signal, float threshold, int attack, int release,int startIndex, int endIndex) {

        attack = (88200/1000) * attack;
        release = (88200/1000) * release;

        boolean isAttacking = false;
        boolean isReleasing = false;
        boolean isHolding = false;

        int attackCount = 0;
        float attackRatio = 1f / attack;

        int releaseCount = release;
        float releaseRatio = 1f / release;

        for (int i = startIndex; i < endIndex; i = i+2) {
            if (!isAttacking && !isHolding && !isReleasing && Math.abs(signal[i]) < threshold) {
                isAttacking = true;
            }

            if (isAttacking) {
                if (Math.abs(signal[i]) > threshold) {
                    signal[i] -= signal[i] * (attackCount * attackRatio);
                    attackCount--;
                } else {
                    signal[i] -= signal[i] * (attackCount * attackRatio);
                    attackCount++;
                }
                if (attackCount == attack + 1) {
                    attackCount = 0;
                    isHolding = true;
                    isAttacking = false;
                } else if (attackCount <= 0) {
                    attackCount = 0;
                    isAttacking = false;
                }
            }

            else if (isHolding) {
                if (Math.abs(signal[i]) > threshold) {
                    isReleasing = true;
                    isHolding = false;
                } else {
                    signal[i] = 0;
                }
            }

            if (isReleasing) {
                if (Math.abs(signal[i]) > threshold) {
                    signal[i] -= signal[i] * (releaseCount * releaseRatio);
                    releaseCount--;
                } else {
                    signal[i] -= signal[i] * (releaseCount * releaseRatio);
                    releaseCount++;
                }
                if (releaseCount <= 0) {
                    isReleasing = false;
                    releaseCount = release;
                } else if (releaseCount == release) {
                    isHolding = true;
                    isReleasing = false;
                }
            }
        }
        return signal;
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
     * @return The post-processed array of float data.
     */
    public float [] getProcessedAudio () {

        return stereoFloatArray;
    }


}
