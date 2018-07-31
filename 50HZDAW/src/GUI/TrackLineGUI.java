package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.ArrayList;

import static java.lang.Math.log10;

public class TrackLineGUI {
    
    private HBox trackLine;
    private String lineName;
    // Name label for the channel
    private Label name;
    private SplitPane parent;
    private Scene mainWindow;
    // The Track attatched to the track line
    private Track track;
    // The offset for each file
    private long start;
    // The Track Mixer
    private MixerSetUp mixerSetUp;
    // The part of the track line that displays the waveforms
    private HBox displayLine;
    // Volume modifier
    private IntegerProperty volume;
    // All files contained in the track line
    private ArrayList<File> files;

    // Pointer shape
    private Rectangle rect;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    /**
     * Constuctor for the Track Line
     * @param name - Name of Track Line
     * @param pane - Parent of the Track Line
     * @param window - Main window
     * @param mixer - mixer for the tracks
     */
    public TrackLineGUI(String name, SplitPane pane, Scene window, MixerSetUp mixer) {
        lineName = name;
        parent = pane;
        mainWindow = window;
        mixerSetUp = mixer;

        files = new ArrayList<>();
        start = 0;
    }

    /**
     * Create an empty Track line
     * @return - Track formatted as an HBox
     */
    public HBox createTrack(){

        // Whole Channel - Settings and waveform
        trackLine = new HBox(20);
        trackLine.prefWidthProperty().bind(mainWindow.widthProperty());
        trackLine.setMinHeight(1);

        // Parent for all settings buttons
        VBox optionsBox = new VBox(5);
        optionsBox.setMinWidth(150);
        optionsBox.setMaxWidth(150);

        // Name label
        name = new Label(lineName);
        
        // Mute and Solo
        HBox muteSolo = new HBox(5);
        Button mute = new Button("Mute");
        mute.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        Button solo = new Button("Solo");
        solo.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        muteSolo.getChildren().addAll(mute, solo);


        // Gain control
        HBox gain = new HBox(5);

        Button gainUp = new Button("Gain +");
        gainUp.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        Button gainDown = new Button("Gain -");
        gainDown.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);

        gain.getChildren().addAll(gainUp, gainDown);

        // Delete Channel
        Button deleteChannel = new Button("Delete channel");
        deleteChannel.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        deleteChannel.setOnAction(event -> {
            // Remove the channel from the scene
            parent.getItems().remove(trackLine);
        });

        // volume volumeSlider
        Slider volumeSlider = new Slider(10, 110, 110);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(20);
        volumeSlider.setMinorTickCount(5);
        volumeSlider.setBlockIncrement(1);

        // Bind volume slider value
        volume = new SimpleIntegerProperty();
        volume.bind(volumeSlider.valueProperty());

        // Adjust the volume of the track, currently does not work real time
        volume.addListener((v, oldValue, newValue) -> {
            float vol = newValue.floatValue()/oldValue.floatValue();
            adjustVolume(vol);

            /*
                float diff = newValue.floatValue()/oldValue.floatValue();
                double deci = 10 * log10(diff);
                float vol = (float) deci;
                adjustVolume(vol);
            */
        });

        // Layout for buttons
        optionsBox.getChildren().addAll(name, muteSolo, gain, deleteChannel, volumeSlider);

        //Timeline
        HBox timelineBox = new HBox(75);
        createTimeline(timelineBox);

        // Box that creates a split between timeline and waveform display
        VBox timelineSplit = new VBox();
        displayLine = new HBox(0);
        timelineSplit.getChildren().addAll(timelineBox, displayLine);

        // Final waveform box with pointer, timeline and waveform display
        VBox finalBox = new VBox();

        // Timeline marker
        rect = new Rectangle();
        rect.setStroke(Color.BLACK);
        rect.setWidth(5);
        rect.setHeight(15);
        rect.setFill(Color.BLACK);
        finalBox.getChildren().addAll(rect, timelineSplit);


        trackLine.getChildren().addAll(optionsBox, finalBox);

        // Allow for files to be dragged and dropped
        acceptDragDrop(trackLine);

        return trackLine;
    }

    /**
     * Adjust the volume of all audio in this track. Values above 1 increase sound, values below decrease sound.
     * Minimum value is 0 max is TBD
     * @param vol - float
     */
    private void adjustVolume(float vol) {
        try {
            track.addProcessing(vol);
            //System.out.println(vol);
        } catch (NullPointerException e) {
            //System.out.println("No track);
        }
    }

    /**
     * Make an object (in this case an HBox) do accept files via drag and drop
     * @param line - an HBox
     */
    private void acceptDragDrop(HBox line){

        // Handler for drag over
        line.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                // If the drag board has at least one file
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        line.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                // if at least one item is dropped
                if (db.hasFiles()) {
                    success = true;
                    // do something for each file dropped
                    for (File file:db.getFiles()) {
                        if (file.getName().endsWith(".wav")) {
                            try {
                                addFile(file);
                            } catch (Exception e) {

                            }
                        }


                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    /**
     * Create the waveform for the audio from it's file
     * @param durationInSeconds - the length in seconds of the file
     * @param file - a .wav file
     * @return - a canvas with the wavewform draw on it
     * @throws Exception
     */
    public Canvas createWaveform(Double durationInSeconds, File file) throws Exception {

        // Make the canvas 10px per second long
        int width = (int) Math.round(durationInSeconds);
        Canvas canvas = new Canvas(width * 10, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = file.getAbsolutePath();

        /*
        AudioInputStream a = AudioSystem.getAudioInputStream(file);
        byte [] stereoByteArray = new byte [(int)file.length()];
        a.read(stereoByteArray);

        WaveformGenerator test = new WaveformGenerator(stereoByteArray, gc);
        test.draw();
        */

        // Draw the waveform from a file
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();

        // Popout waveform editor window on click
        canvas.setOnMouseClicked(e -> {
            WaveformEditor w = new WaveformEditor(width, file, durationInSeconds);
        });

        //to set starting position for waveform
        canvas.setTranslateX(0);
        //10 pixels = 1 second or 1000 ms
        //1 pixel = 0.1 second or 100ms
        //if setting position with ms, /100

        return canvas;
    }

    /**
     *
     * @param file
     * @throws Exception
     */
    public void addFile(File file) throws Exception {

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        double durationInSeconds = (frames+0.0)/format.getFrameRate();

        if (files.size() == 0) {
            System.out.println("I tried to add a track");
            track = mixerSetUp.addTrack(file.getName(), file, (volume.get()/100));
            name.textProperty().setValue(file.getName());
        } else {
            System.out.println("I tried to add to a existing track");
            track.addAudioTrackData(file, 0);
        }

        Canvas canvas = createWaveform(durationInSeconds, file);

        canvas.setTranslateX(0);

        files.add(file);

        start += durationInSeconds;
        System.out.println("Start at :" + start);

        displayLine.getChildren().add(canvas);
    }

    public HBox createTimeline(HBox box) {

        for (int i = 0; i < 1000; i += 10) {
            Label label = new Label(i + "");
            label.setMinWidth(25);
            label.setMinHeight(25);
            label.setMinHeight(25);
            label.setMinHeight(25);

            box.getChildren().add(label);
        }

        return box;
    }

    public void createPointer(int width) {

        rect = new Rectangle();
        rect.setStroke(Color.BLACK);
        rect.setWidth(5);
        rect.setHeight(15);
        rect.setFill(Color.BLACK);

        TT = new TranslateTransition(Duration.seconds(width), rect);
        TT.setToX(width * 10);
        TT.setInterpolator(Interpolator.LINEAR);
    }

    public void addPointer() {

    }

    public TranslateTransition getTT() {
        return TT;
    }

    public Rectangle getRect() {
        return rect;
    }
}
