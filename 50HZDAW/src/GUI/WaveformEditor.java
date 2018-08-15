package GUI;

import Audio.*;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.util.ArrayList;


public class WaveformEditor {

    private double durationInSeconds;
    private int index;
    private File file;
    private Track track;
    private StackPane waveformStack;
    private WaveformCanvas waveformCanvas;
    private Canvas waveform;
    private double pixelRatio;
    
    private TrackLineGUI trackLineGUI;

    public WaveformEditor(double fileLength, int index, File f, Track track, StackPane waveformStack, WaveformCanvas canvas, TrackLineGUI TLG){
        durationInSeconds = fileLength/100;
        this.index = index;
        file = f;
        this.track = track;
        this.waveformStack = waveformStack;
        waveformCanvas = canvas;
        waveform = waveformCanvas.getCanvas();
        trackLineGUI = TLG;
        pixelRatio = waveformCanvas.getPixelRatio();
        createEditor();
    }

    /**
     * Creates an editor window for the specified AudioData object. From here the user can
     * get information about the file, make arrangement adjustments and apply effects.
     *
     */
    public void createEditor() {

        double width = durationInSeconds;
        Canvas canvas = new Canvas(width, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = file.getAbsolutePath();
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();

        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Waveform Editor");
        window.setMinWidth(350);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_LEFT);
        ScrollPane sp = new ScrollPane();
        sp.setContent(canvas);

        VBox fileInformation = new VBox(10);
        Label informationLabel = new Label("File Information");
        informationLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        fileInformation.getChildren().add(informationLabel);
        VBox fileArrangement = new VBox(10);
        Label arrangementLabel = new Label("File Arrangement");
        arrangementLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        fileArrangement.getChildren().add(arrangementLabel);
        VBox fileEffects = new VBox(10);
        Label effectsLabel = new Label("File Effects");
        effectsLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        fileEffects.getChildren().add(effectsLabel);


        Button addFadeIn = new Button("Add fade in");
        addFadeIn.setOnAction(e -> {
            addFadeIn();
        });

        Button addFadeOut = new Button("Add fade out");
        addFadeOut.setOnAction(e -> {
            addFadeOut();
        });

        Button addDistortion = new Button("Add distortion");
        addDistortion.setOnAction(e -> {
            addDistortionEffect();
        });

        Button addFaze = new Button("Add faze");
        addFaze.setOnAction(e -> {
            addFaze();
        });

        Button addDelayEffect = new Button("Add delay");
        addDelayEffect.setOnAction(e -> {
            addDelayEffect();
        });

        Button addPaddingButton = new Button("Set starting position");
        addPaddingButton.setOnAction(e -> {
            setStartTime();
        });

        Button cutButton = new Button("Cut");
        cutButton.setOnAction(e -> {
            cut();
        });

        Button trimButton = new Button("Trim");
        trimButton.setOnAction(e -> {
            trim();
        });

        Button duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(e -> {
            duplicate();
        });


        Button zoomButton = new Button("Zoom Out");
        zoomButton.setOnAction(e -> canvasZoom(canvas, filePath, true));

        Button zoomOutButton = new Button("Zoom");
        zoomOutButton.setOnAction(e -> canvasZoom(canvas, filePath, false));

        Label selectedTime = new Label("Selected time is: ");

        String lengthString = "";
        lengthString = lengthString.format("File Duration: %.2f seconds", durationInSeconds/10);
        Label fileName = new Label("File Name: " + formatName(file.getName()));
        Label fileLength = new Label(lengthString);

        fileInformation.getChildren().addAll(fileName, fileLength, zoomOutButton, zoomButton, selectedTime);
        fileArrangement.getChildren().addAll(addPaddingButton, cutButton, trimButton, duplicateButton);
        fileEffects.getChildren().addAll(addFadeIn, addFadeOut, addDistortion, addFaze, addDelayEffect);

        HBox buttonDivider = new HBox(20);
        buttonDivider.getChildren().addAll(new Region(),fileInformation, fileArrangement, fileEffects, new Region());
        layout.getChildren().addAll(sp, buttonDivider);


        canvas.setOnMouseClicked(e -> {
            double x = e.getX() / canvas.getWidth();
            double d = (x * durationInSeconds);
            double roundedOneDigitX = Math.round(d * 10) / 10.0;
            selectedTime.setText("Selected time: " + roundedOneDigitX + " seconds");

        });

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.show();

    }

    /**
     * Apply a zoom-in or zoom-out effect to the WaveformEditor's canvas
     *
     * @param canvas The canvas to apply the transformation to
     * @param filePath The filepath for the file object
     * @param increase Determined by if the user has selected zoom-in or zoom-out
     * @return The same array with specified gap
     */
    public static void canvasZoom(Canvas canvas, String filePath, boolean increase) {

        try {
            canvas.setHeight(100);
            if (increase) {
                canvas.setWidth(canvas.getWidth() / 2);
            } else {
                canvas.setWidth(canvas.getWidth() * 2);
            }
            GraphicsContext g = canvas.getGraphicsContext2D();
            WaveformGenerator w = new WaveformGenerator(new File(filePath), g);
            w.draw();
        } catch (RuntimeException e) {
            System.out.println("Reached zoom maximum");
        }


    }

    /**
     * Adds a fade-in effect to an AudioData object. A window will pop up where the user can
     * enter the desired time for the effect to be applied to
     */
    public void addFadeIn() {

        FadeManager FM = new FadeManager();
        AudioData data = track.getAudioData().get(index);

        double position = DelayBox.Display(0, pixelRatio, "end time for fade-in (in ms)");
        position = position * pixelRatio;
        position = position * 88200;

        float[] newFloat = FM.addFadeIn((int) position, data.getStereoFloatArray());
        data.setStereoFloatArray(newFloat);

        track.addDataToTrack();

    }

    /**
     * Adds a fade-out effect to an AudioData object. A window will pop up where the user can
     * enter the desired time for the effect to be applied to
     */
    public void addFadeOut() {

        FadeManager FM = new FadeManager();
        AudioData data = track.getAudioData().get(index);

        double position = DelayBox.Display(0, pixelRatio, "start time for fade-out (in ms)");
        position = position * pixelRatio;
        position = position * 88200;

        float[] newFloat = FM.addFadeOut((int) position, data.getStereoFloatArray());
        data.setStereoFloatArray(newFloat);

        track.addDataToTrack();

    }

    /**
     * Adds a distortion effect to an AudioData object. A window will pop up where the user can
     * enter the desired threshold for the effect
     */
    public void addDistortionEffect() {

        float threshold = (float) DelayBox.Display(0, pixelRatio, "value for distortion (largest value is: " + findMinAndMax() + ")");

        track.getAudioData().get(index).setStereoFloatArray(distortion(track.getAudioData().get(index).getStereoFloatArray(), threshold));
        track.addDataToTrack();

    }

    /**
     * Adds a delay effect to an AudioData object. A window will pop up where the user can
     * enter the desired parameters for the effect
     */
    public void addDelayEffect() {

        ArrayList<Float> list = PedalGUI.Display();

        int delay = Math.round(list.get(0));
        int feedback = Math.round(list.get(1));

        track.getAudioData().get(index).setStereoFloatArray(delayLoop(track.getAudioData().get(index).getStereoFloatArray(), delay, feedback, list.get(2)));
        track.getAudioData().get(index).setFinish();
        track.addDataToTrack();

    }

    /**
     * Finds the largest number that is stored in a stereo float array, regardless of whether
     * the value is positive or negative
     *
     * @return The largest number stored in the array
     */
    public float findMinAndMax() {

        float min = 0;
        float max = 0;

        float[] f = track.getAudioData().get(index).getStereoFloatArray();
        for (int i = 0; i < f.length; i++) {

            if (f[i] > max) {
                max = f[i];
            }
            else if (f[i] < min) {
                min = f[i];
            }
        }

        float negative = min * -1f;
        if (negative > max) {
            max = negative;
        }
        System.out.println(min);
        System.out.println(max);

        return max;
    }

    /**
     * Adds a faze effect to an AudioData object. A window will pop up where the user can
     * enter the desired speed for the effect
     */
    public void addFaze() {

        FadeManager FM = new FadeManager();
        AudioData data = track.getAudioData().get(index);

        double speed = DelayBox.Display(0, pixelRatio, "speed for faze effect (recommended 0 - 250)");

        float[] newFloat = FM.fadeInAndOut(data.getStereoFloatArray(), (int) speed);
        data.setStereoFloatArray(newFloat);

        track.addDataToTrack();


    }

    public void cut(){

        ArrangementHelper AH = new ArrangementHelper();
        AudioData data = track.getAudioData().get(index);

        double split = DelayBox.Display(0, pixelRatio, "time for split");
        split = split * 88.2;

        ArrayList<float[]> list = AH.cutArray(data.getStereoFloatArray(), (int) split);
        float[] firstArray = list.get(0);
        float[] secondArray = list.get(1);

        data.setStereoFloatArray(firstArray);
        //TODO finish

    }

    /**
     * Trims a block of audio between to user-given values. The original waveform is removed
     * from the GUI and a new waveform is generated and added.
     */
    public void trim() {

        ArrangementHelper AH = new ArrangementHelper();
        AudioData data = track.getAudioData().get(index);

        double startX = DelayBox.Display(0, pixelRatio, "start time for audio cut (in ms)");
        double endX = DelayBox.Display(0, pixelRatio, "end time for audio cut (in ms)");
        double pos = waveform.getTranslateX();
        double diff = endX - startX;
        double end = endX * 88.2;
        double start = startX * 88.2;

        float[] newArray = AH.getRangeOfArrayValues((int) start, (int) end, data.getStereoFloatArray());
        data.setStereoFloatArray(newArray);



        track.addDataToTrack();

        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        int minValue = - ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1));    // calculate min & max representable int value for n-bit number.
        int maxValue = ((int)Math.pow(2, audioFormat.getSampleSizeInBits()-1)) - 1;


        waveformStack.getChildren().remove(waveform);
        waveform = new Canvas(diff * pixelRatio, 150);
        GraphicsContext gc = waveform.getGraphicsContext2D();
        ByteToFloat BTF = new ByteToFloat();
        byte[] b = BTF.floatToByteArray(newArray, minValue, maxValue);
        WaveformGenerator wf = new WaveformGenerator(b, gc);
        wf.setWaveAverageColor(Color.MIDNIGHTBLUE);
        wf.setBackgroundColor(Color.color(1,1,1, 0.2));
        wf.draw();
        waveform.setTranslateX((startX * pixelRatio) + pos);
        waveformCanvas.setCanvas(waveform);


        //TODO add mouse listeners to new waveform
    }


    /**
     * Sets the starting time for an AudioData object. A window will pop up where the user can
     * enter the desired time
     */
    public void setStartTime() {

        double position = DelayBox.Display(waveform.getTranslateX()/pixelRatio, pixelRatio, "desired starting time (in ms)");
        System.out.println("Delay = " + (position/pixelRatio) + "ms");
        System.out.println(position + " is position");
        waveform.setTranslateX(position * pixelRatio);
        long delay = (long) position;
        System.out.println(delay);
        track.moveAudioFile(index, delay);
    }


    /**
     * Adds a copy of the audio block to the track. Since the file object is being passed as a
     * parameter, only the original audio file will be duplicated, any applied effects will
     * not be
     */
    public void duplicate() {

        try {
            trackLineGUI.addFile(file);
        } catch (Exception e) {}
    }


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

    /**
     * Formats a string, namely the name of the file object, so that each line is
     * exactly 15 characters in length
     *
     * @return A string with formatted line length
     */
    public String formatName(String unformattedString) {

        int count = 0;
        String newStr = "";
        String[] str = unformattedString.split("");
        for (int i = 0; i < unformattedString.length(); i++) {
            if (count == 15) {
                newStr += System.lineSeparator();
                count = 0;
                newStr += str[i];
                count++;
            }
            else {
                newStr += str[i];
                count++;
            }
        }
        return newStr;
    }

    public float[] delayLoop(float[] test, int delay, int layers, float fadeOut) {

        float[] newTest = new float[test.length + (delay * layers)];
        int delayFixedValue = delay;

        float[] layer = new float[test.length];

        for (int i = 0; i< test.length; i++ ) {
            layer[i] = test[i];
        }



        for (int i = 0; i < layers; i++) {
            newTest = addOneDelay(test, layer, delay,fadeOut);
            test = newTest;
            delay = delayFixedValue + delay;
            fadeOut = 1 * fadeOut;
        }
        return newTest;
    }

    private float[] addOneDelay(float[] originalFloats, float [] layer, int delay, float fadeOut) { ;

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


    public float averageGain(float[] floats) {
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

}

