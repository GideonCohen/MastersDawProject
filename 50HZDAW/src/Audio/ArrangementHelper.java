package Audio;

import java.util.ArrayList;

/**
 * Class to handle simple audio arrangement functionality such as editing the length of
 * an audio track, adding padding to the start/end of an audio track etc.
 * @author Thomas Martin
 */

public class ArrangementHelper {

    /**
     * Create a new array taking all elements up to the specified index
     *
     * @param end      The number of elements to take from the first array
     * @param oldArray The original array to be processed
     * @return The new array with specified elements
     */
    public byte[] getStartOfArray(int end, byte[] oldArray) {

        byte[] newArray = new byte[end];

        for (int i = 0; i < end; i++) {
            newArray[i] = oldArray[i];
        }
        return newArray;
    }

    public ArrayList<byte[]> splitArray(int splitIndex, byte[] bytes) {

        ArrayList<byte[]> splitArrays = new ArrayList<>();
        byte[] arrayOne = new byte[splitIndex];
        byte[] arrayTwo = new byte[bytes.length-splitIndex];

        int count = 0;

        for (int i = 0; i < splitIndex; i++) {
            arrayOne[i] = bytes[i];
        }

        for (int i = splitIndex; i < bytes.length; i++) {
            arrayTwo[count] = bytes[i];
            count++;
        }

        splitArrays.add(arrayOne);
        splitArrays.add(arrayTwo);

        return splitArrays;
    }

    public byte[] duplicateArray(byte[] oldArray) {

        byte[] duplicate = oldArray;
        return addTwoArrays(oldArray, duplicate);


    }

    /**
     * Create a new array taking all elements within the specified range
     *
     * @param start    The lower limit index to take elements from
     * @param end      The upper limit index to take elements from
     * @param oldArray The original array to be processed
     * @return The new array with specified elements
     */
    public byte[] getRangeOfArrayValues(int start, int end, byte[] oldArray) {

        int newSize = end - start;
        if (newSize % 2 != 0) {
            //to ensure that number of bytes in array is divisible by framesize (2)
            newSize++;
        }
        byte[] newArray = new byte[newSize];
        int x = 0;

        for (int i = start; i < end; i++) {
            newArray[x] = oldArray[i];
            x++;
        }

        return newArray;
    }

    /**
     * Update an array by removing a set of elements in the specified range
     *
     * @param start The lower limit index to start removing elements from
     * @param end   The upper limit index to stop removing elements
     * @param array The original array to be processed
     * @return The same array with specified gap
     */
    public byte[] removeRangeOfValues(int start, int end, byte[] array) {

        for (int i = start; i < end; i++) {
            array[i] = 0;
        }

        return array;
    }

    /**
     * Create a new array by adding to arrays together
     *
     * @param arrayOne The first array to add
     * @param arrayTwo The second array to add
     * @return The new array with the combination of the two original arrays
     */
    public byte[] addTwoArrays(byte[] arrayOne, byte[] arrayTwo) {

        int newSize = arrayOne.length + arrayTwo.length;
        byte[] newArray = new byte[newSize];
        int i = 0;

        for (byte b : arrayOne) {
            newArray[i] = b;
            i++;
        }

        for (byte b : arrayTwo) {
            newArray[i] = b;
            i++;
        }

        return newArray;
    }

    public int getFirstElementPosition(byte[] array) {

        int position = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) {
            }
            else {
                position = array[i];
                break;
            }
        }

        getFirstMS(position);
        System.out.println(position);
        return position;
    }

    public double getFirstMS(int position) {

        double ms = position * 176.4;
        System.out.println(ms);
        return ms;
    }

    /**
     * Add a set of byte arrays together
     *
     * @param set A 2D array containing a set of byte arrays
     * @return The new array with elements from the 2D array
     */
    public byte[] addSetOfArrays(byte[][] set) {

        int newSize = 0;
        int count = 0;

        for (byte[] b : set) {
            newSize += b.length;
        }

        byte[] newArray = new byte[newSize];

        for (byte[] array : set) {
            for (byte b : array) {
                newArray[count] = b;
                count++;
            }
        }

        return newArray;

    }

    /**
     * Add a set number of empty elements to the start of an array to provide padding
     *
     * @param padding  The number of empty elements to add to the array
     * @param oldArray The original array to be processed
     * @return The new array with the same elements and padding at the start
     */
    public byte[] addPaddingToStart(int padding, byte[] oldArray) {

        int newSize = padding + oldArray.length;
        byte[] newArray = new byte[newSize];
        int x = padding;

        for (int i = 0; i < padding; i++) {
            newArray[i] = 0;
        }

        for (byte b : oldArray) {
            newArray[x] = b;
            x++;
        }

        return newArray;
    }

    /**
     * Add a set number of empty elements at the end of an array to provide padding
     *
     * @param padding  The number of empty elements to add to the array
     * @param oldArray The original array to be processed
     * @return The new array with the same elements and padding at the end
     */
    public byte[] addPaddingToEnd(int padding, byte[] oldArray) {

        int newSize = padding + oldArray.length;
        byte[] newArray = new byte[newSize];
        int x = 0;

        for (byte b : oldArray) {
            newArray[x] = b;
            x++;
        }

        for (int i = x; i < newSize; i++) {
            newArray[i] = 0;
        }

        return newArray;
    }

    public byte[][] combineSetsOfArrays(byte[][] setOne, byte[][] setTwo) {

        int numberOfSets = setOne.length + setTwo.length;
        int numberOfElements = 0;
        int count = 0;

        for (byte[] bytes : setOne) {
            for (byte b : bytes) {
                numberOfElements++;
            }
        }
        for (byte[] bytes : setTwo) {
            for (byte b : bytes) {
                numberOfElements++;
            }
        }

        byte[][] newSet = new byte[numberOfSets][numberOfElements];

        //TODO finish
        return newSet;
    }
}