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
    private int bitDepth;


    public StereoSplit(float [] stereoFloatArray) {

        this.stereoFloatArray = stereoFloatArray;
        this.bitDepth = bitDepth;
        leftFloatArray = new float [stereoFloatArray.length];
        rightFloatArray = new float [stereoFloatArray.length];


    }


    /**
     * Split a stereo float arrays in to two mono left and right channels represented as new float arrays.
     * For 16-bit stereo audio files, each sample will take 2 bytes. (24-bit may be different).
     *
     */

    public void split() {

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
     * Set panning positions of stereo audio.
     */

    public void setPanning (float [] monoArray, float gain) {

        for(int i = 0; i < monoArray.length; i++) {

            monoArray [i] = monoArray [i] * gain;
        }
    }



    /**
     * Converge two mono array channels (left & right) into one stereo array channel.
     * This new stereo array will be the result of processed audio (panning/volume/EQ).
     */

    public float [] convergeMonoArrays () {

        setPanning(leftFloatArray, 1.0f);   // reduce values of left to emphasise right (vice versa)
        setPanning(rightFloatArray, 1.0f);


        for (int i = 0; i < stereoFloatArray.length; i += 2) {

            try {
                stereoFloatArray[i] = leftFloatArray[i];
                stereoFloatArray[i + 1] = rightFloatArray[i + 1];
            }
            catch (ArrayIndexOutOfBoundsException aioobe) {
                System.out.println(aioobe.getMessage());
            }

        }

        return stereoFloatArray;

    }


    /**
     * Get left channel.
     */

    public float [] getLeftArray () {

        return leftFloatArray;

    }


    /**
     * Get right channel.
     */

    public float [] getRightArray () {

        return rightFloatArray;

    }








}
