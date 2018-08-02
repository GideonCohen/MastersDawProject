package Audio;

/**
 * Class can convert files from 16-bit to 24-bit or vice versa.
 */

public class BitDepthConverter {

    public BitDepthConverter () {


    }

    /**
     * Convert 16-bit array in to 24-bit array (can also be done using bit shift). Simply add 0 (8-bit number) to least significant bits.
     * @param arrayToConvert
     * @return
     */

    public byte [] convert16BitTo24Bit (byte [] arrayToConvert) {


        byte [] convertedArray = new byte [arrayToConvert.length + (arrayToConvert.length/2)];   // 24-bit array is 1.5 times size of 16-bit array.

        int i = 0;
        for(int j = 0; j < arrayToConvert.length; j+=2) {

            convertedArray[i] = arrayToConvert [j];
            convertedArray[i + 1] = arrayToConvert [j + 1];
            convertedArray[i + 2] = 0;

            i += 3;
        }

        return null;

    }



    /**
     * Convert 24-bit array in to 16-bit array (can also be done using bit shift). Simply remove every third number in array
     * (single byte removed from data point) may reduce quality of audio.
     * @param arrayToConvert
     * @return
     */

    public byte [] convert24BitTo16Bit (byte [] arrayToConvert) {


        byte [] convertedArray = new byte [(arrayToConvert.length/3) * 2];   // 24-bit array is 1.5 times size of 16-bit array.

        for(int i = 0; i < arrayToConvert.length; i+=3) {   // omit every 3rd byte.

            convertedArray [i] = arrayToConvert [i];
            convertedArray [i + 1] = arrayToConvert [i + 1];
            
        }

        return null;

    }





}
