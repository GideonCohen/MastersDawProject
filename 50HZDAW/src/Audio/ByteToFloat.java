package Audio;

import static java.lang.Math.log;

/**
 * Class converts an byte array to a float array and vice versa. Used for greater accuracy in audio processing.
 */

public class ByteToFloat {

    private int minValue;
    private int maxValue;


    public ByteToFloat () {

    }

    /**
     * Convert byte array of audio into array of floats for processing such as volume control.
     * @param byteArray - audio array
     * @param maxValue - Maximum signed value
     * @return - Float array float[]
     */
    public float [] byteToFloatArray (byte [] byteArray, int maxValue) {

        double bitSize = (log(maxValue+1)/log(2)) + 1;
        //System.out.println( bitSize +  " Testing line");

        // Check for 16 bit
        if (bitSize == 16.0) {
            float[] floatArray = new float[byteArray.length/2];

            for (int i = 0; i < byteArray.length; i += 2) {
                // convert bytes i and i +1 to 16bit values, combine and the normalise between -1 and 1
                float f = ((int) ((byteArray[i]) & 0xff) | (int) ((byteArray[i + 1] << 8))) / (float) maxValue;
                if (f > 1) f = 1;   // force normalize values
                if (f < -1) f = -1;
                floatArray[i / 2] = f;
            }

            return floatArray;
        // check for 24 bit
        } else if (bitSize == 24.0) {
            float[] floatArray = new float[byteArray.length/3];
            //System.out.println(byteArray.length/3);
            //System.out.println(floatArray.length);
            for (int i = 0; i <= byteArray.length; i += 3) {
                try {
                    // convert bytes i, i+1 and i+2 to 24bit values and combine
                    int int24 = ((int) (byteArray[i] & 0xff) | (int) ((byteArray[i + 1] & 0xff)<< 8) | (int) ((byteArray[i + 2])<< 16) );
                    // Normalise between -1 and 1
                    float f = int24 / (float) maxValue;
                    if (f > 1) f = 1;   // force normalize values
                    if (f < -1) f = -1;
                    floatArray[i / 3] = f;
                } catch (IndexOutOfBoundsException e) {
                    //System.out.println("Index out of bounds exception at Float[]:" + i/'3' + " Byte[]: " + i);
                }
            }

            return floatArray;
        } else {
            // if not 16 or 24 bit, invalid size
            System.out.println("INVALID BIT SIZE");
            return null;
        }
    }


    /**
     * Convert float array back to byte array. New byte array will be post-audio-processing.
     * @param floatArray - array to convert back
     * @param minValue - Minimum signed value
     * @param maxValue - Maximum signed value
     * @return
     */
    public byte [] floatToByteArray (float [] floatArray, int minValue, int maxValue) {

        double bitSize = (log(maxValue+1)/log(2)) + 1;
        //System.out.println( bitSize +  " Testing line 2");

        if (bitSize == 16.0) {
            byte[] byteArray = new byte[floatArray.length * 2];

            int i = 0;
            for (float sample : floatArray) {
                // de-normalise values
                float f = sample * maxValue;
                if (f > maxValue) f = maxValue;
                if (f < minValue) f = minValue;
                int value = (int) f;
                // Break value down from 16 bit to 2 differnt bytes
                byteArray[i++] = (byte) ((value) & 0xff);
                byteArray[i++] = (byte) ((value >> 8) & 0xff);
            }

            return byteArray;
        }

        else if (bitSize == 24.0) {
            byte[] byteArray = new byte[floatArray.length * 3];

            int i = 0;
            for (float sample : floatArray) {
                // de-normalise values
                float f = sample * maxValue;
                if (f > maxValue) f = maxValue;
                if (f < minValue) f = minValue;
                int value = (int) f;
                // Break value down from 16 bit to 3 differnt bytes
                byteArray[i++] = (byte) ((value));
                byteArray[i++] = (byte) ((value >> 8) & 0xff);
                byteArray[i++] = (byte) ((value >> 16) & 0xff);
            }

            return byteArray;
        } else {
            // If the bit size is not 16 or 24 bit
            System.out.println("PROBLEM");
            return null;
        }

    }


    /**
     * Get min representable value.
     * @return minValue
     */
    public int getMinValue () {
        return minValue;
    }

    /**
     * Get max representable value.
     * @return maxValue
     */
    public int getMaxValue () {
        return maxValue;
    }

}
