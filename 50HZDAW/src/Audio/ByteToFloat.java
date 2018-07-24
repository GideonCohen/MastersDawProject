package Audio;

import static java.lang.Math.*;

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
     */

    /**
     * Convert a 16bit or 24bit array into an array of floats between -1 and 1
     * @param byteArray - the array to be converted
     * @param maxValue - max value represented by 16 or 24 signed bits
     * @return
     */
    public float [] byteToFloatArray (byte [] byteArray, int maxValue) {

        // Checks for 16 or 24 bit
        double bitSize = (log(maxValue+1)/log(2)) + 1;
        //System.out.println( bitSize +  " Testing line");

        // Transformation for 16 bit array
        if (bitSize == 16.0) {
            // 2 Bytes = 16 bits - new array is 1/2 the size
            float[] floatArray = new float[(byteArray.length/2)];

            for (int i = 0; i < byteArray.length; i += 2) {
                /*
                The higher value is shifted left by 8 points (1 byte) and they are then added together.
                Finally that value is normalised to between 1 and -1
                 */
                // byte conversion
                int int16 = (int) ((byteArray[i]) & 0xff) | (int) ((byteArray[i + 1] << 8));
                // normalisation
                float f = int16 / (float) maxValue;
                // Restrict any values that would be too high to prevent clipping
                if (f > 1) f = 1;
                if (f < -1) f = -1;
                // add the converted value to the float array
                floatArray[i / 2] = f;
            }

            return floatArray;

        // Transformation for 24 bit array
        } else if (bitSize == 24.0) {
            // 3 Bytes = 24 bits - new array is 1/3 the size
            float[] floatArray = new float[(byteArray.length/3)];

            for (int i = 0; i <= byteArray.length; i += 3) {
                try {
                    /*
                    The higher value is shifted left by 8 points (1 byte), then 16 points (2 bytes). They are then added together.
                    Finally that value is normalised to between 1 and -1
                    */
                    // Byte conversion
                    int int24 = ((int) (byteArray[i] & 0xff) | (int) ((byteArray[i + 1] & 0xff)<< 8) | (int) ((byteArray[i + 2])<< 16));
                    // Normalisation
                    float f = int24 / (float) maxValue;
                    // Restrict any values that would be too high to prevent clipping
                    if (f > 1) f = 1;
                    if (f < -1) f = -1;
                    // add the converted value to the float array
                    floatArray[i / 3] = f;

                } catch (IndexOutOfBoundsException e) {
                    // Catches index exceptions that currently occur for unknown reasons
                    System.out.println("Index out of bounds exception at Float[]:" + i/'3' + " Byte[]: " + i);
                }
            }
            return floatArray;
        } else {
            // If you get here your array is neither 16 nor 24 bit
            System.out.println("INVALID BIT SIZE");
            return null;
        }
    }

    /**
     * Converts an array of floats back to either byte array. The conversion is based on if the file is 16 or 24 bi
     * @param floatArray - the array of floats to convert
     * @param minValue - the minimum value for 16 or 24 bits as an integer (generally -maxvalue - 1)
     * @param maxValue - the maximum value for 16 or 24 bits as an integer
     * @return - a byte array for 16 bit or 24 bit audio
     */
    public byte [] floatToByteArray (float [] floatArray, int minValue, int maxValue) {

        // Checks the bit size for the byte array
        double bitSize = (log(maxValue+1)/log(2)) + 1;
        //System.out.println( bitSize +  " Testing line 2");

        // Transformation to a 16 bit format
        if (bitSize == 16.0) {
            // 2 Bytes = 16 bits - new array is x2 the size
            byte[] byteArray = new byte[(floatArray.length * 2)];

            int i = 0;
            // for each float
            for (float sample : floatArray) {
                // undo the previous normalisation
                float f = sample * maxValue;
                // restricting the values to prevent clipping
                if (f > maxValue) f = maxValue;
                if (f < minValue) f = minValue;
                // convert to int
                int value = (int) f;
                // The first value is the lower part of the 16 bit number
                byteArray[i++] = (byte) ((value) & 0xff);
                // The second half is shifted 8 points to the left
                byteArray[i++] = (byte) ((value >> 8) & 0xff);
            }

            return byteArray;


        // Transformation to a 24 bit format
        } else if (bitSize == 24.0) {
            // 3 Bytes = 24 bits - new array is x3 the size
            byte[] byteArray = new byte[(floatArray.length * 3)];

            int i = 0;
            for (float sample : floatArray) {
                // undo the previous normalisation
                float f = sample * maxValue;
                // restricting the values to prevent clipping
                if (f > maxValue) f = maxValue;
                if (f < minValue) f = minValue;
                // convert to int
                int value = (int) f;
                // The first value is the lowest part of the 24 bit number
                byteArray[i++] = (byte) ((value));
                // The second value is the middle section and shifted 8 points to the left
                byteArray[i++] = (byte) ((value >> 8) & 0xff);
                // The third value is the top of the 24 bit number and shifted 16 points to the left
                byteArray[i++] = (byte) ((value >> 16) & 0xff);
            }

            return byteArray;
        } else {
            // If you reach here then your float array cannot convert to a valid format
            System.out.println("PROBLEM");
            return null;
        }

    }


    /**
     * @return - Minimum int value for any byte in the array
     */
    public int getMinValue () {
        return minValue;
    }


    /**
     * @return - maximum int value for any byte in the array
     */
    public int getMaxValue () {
        return maxValue;
    }

}
