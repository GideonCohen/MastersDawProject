package Audio;

/**
 * Splits a stereo audio file in to two mono arrays (left and right). Also can converge two mono arrays in to one stereo array.
 *
 */

public class StereoSplit {

    private byte [] stereoByteArray;
    private float [] stereoFloatArray;

    private float [] leftFloatArray;
    private float [] rightFloatArray;


    public StereoSplit() {


    }


    /**
     * Split a stereo float array in to two mono left and right channels represented as new float arrays.
     * The first value of a stereo float array will represent a left channel value and the second value will
     * represent a right channel value. This pattern is followed for the whole of the array.
     * @param trackBuffer The array of float data to be split.
     */
    public void split(float [] trackBuffer) {

        this.stereoFloatArray = trackBuffer;
        leftFloatArray = new float [stereoFloatArray.length];
        rightFloatArray = new float [stereoFloatArray.length];

        for (int i = 0; i < stereoFloatArray.length; i += 2) {

                try {
                    leftFloatArray[i] = stereoFloatArray[i];
                } catch (ArrayIndexOutOfBoundsException aioobe) {

                    System.out.println(aioobe.getMessage());           // 16-bit split

                }

                try {
                    rightFloatArray[i + 1] = stereoFloatArray[i + 1];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    System.out.println(aioobe.getMessage());
                }


        }
        
    }

    /**
     * Set panning positions of stereo audio. Multiply each value of the array by a factor of x.
     * @param monoArray The array to be multiplied.
     * @param gain The amount to multiply by.
     */
    public void setPanning (float [] monoArray, float gain) {

        for(int i = 0; i < monoArray.length; i++) {
            monoArray [i] = monoArray [i] * gain;
        }
    }



    /**
     * Converge two mono array channels (left & right) into one stereo array channel.
     * This new stereo array will be the result of pan-processed audio.
     * @param leftGain The value to multiply the left channel by.
     * @param rightGain The value to multiply the right channel by.
     */
    public void convergeMonoArrays (float leftGain, float rightGain) {

        System.out.println("LEFT: " + leftGain);
        System.out.println("RIGHT: " + rightGain);

        setPanning(leftFloatArray, leftGain);   // reduce values of left to emphasise right (vice versa)
        setPanning(rightFloatArray, rightGain);

        for (int i = 0; i < stereoFloatArray.length; i += 2) {

            try {
                stereoFloatArray[i] = leftFloatArray[i];
                stereoFloatArray[i + 1] = rightFloatArray[i + 1];
            }
            catch (ArrayIndexOutOfBoundsException aioobe) {
                System.out.println(aioobe.getMessage());
            }

        }

    }


    /**
     * Get left channel.
     * @return The left channel (mono).
     */
    public float [] getLeftArray () {

        return leftFloatArray;

    }


    /**
     * Get right channel.
     * @return The right channel (mono).
     */
    public float [] getRightArray () {

        return rightFloatArray;

    }






}
