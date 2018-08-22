package Audio;

import GUI.ErrorMessageGUI;

public class FadeManager {

    /**
     * Add a fade-in effect to a piece of audio represented as an array of floats
     *
     * @param fadeInEnd The index position that the effect will finish by
     * @param stereoFloatArray The array of floats to be processed
     *
     * @return An updated float array with the applied transformation
     */
    public float[] addFadeIn(int fadeInEnd, float[] stereoFloatArray) {

        float scale = 1.0f / fadeInEnd;
        float currentVol = 0.0f;

        try {

            for (int i = 0; i < fadeInEnd; i++) {
                stereoFloatArray[i] = stereoFloatArray[i] * currentVol;
                currentVol += scale;
            }
        }
        catch (ArrayIndexOutOfBoundsException a) {
            ErrorMessageGUI.Display();
        }
        return stereoFloatArray;
    }

    /**
     * Add a fade-out effect to a piece of audio represented as an array of floats
     *
     * @param startPosition The location to start the fade from as an index position
     * @param stereoFloatArray The array of floats to be processed
     *
     * @return An updated float array with the applied transformation
     */
    public float[] addFadeOut(int startPosition, float[] stereoFloatArray) {

        int diff = stereoFloatArray.length - startPosition;
        float scale = 1.0f / diff;
        float currentVol = 1.0f;

        if (diff >= 1) {
            for (int i = startPosition; i < stereoFloatArray.length; i++) {
                stereoFloatArray[i] = stereoFloatArray[i] * currentVol;
                currentVol -= scale;
            }
        }
        else {
            ErrorMessageGUI.Display();
        }


        return stereoFloatArray;

    }

    /**
     * Add an oscillatory fade pattern to a piece of audio represented as an array of floats
     * @param stereoFloatArray The array of floats to be processed
     * @param speed The speed of the oscillatory effect
     *
     * @return An updated float array with the applied transformation
     */
    public float[] fadeInAndOut(float[] stereoFloatArray, int speed) {

        float count = 0.00001f * speed;
        boolean up = true;

        if (count <= 1) {
            for (int i = 0; i < stereoFloatArray.length; i++) {
                if (count >= 1.0f) {
                    up = false;
                }
                if (count <= 0.0f) {
                    up = true;
                }
                stereoFloatArray[i] = stereoFloatArray[i] * count;

                if (up) {
                    count += 0.00001f * speed;
                }
                if (!up) {
                    count -= 0.00001f * speed;
                }
            }
        }
        else {
            ErrorMessageGUI.Display();
        }

        return stereoFloatArray;
    }
}
