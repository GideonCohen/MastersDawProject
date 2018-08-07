package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.ArrayList;



public class TrackLineGUI {

    private HBox trackLine;
    private String lineName;
    // Name label for the channel
    private Label name;
    private SplitPane parent;
    private Scene mainWindow;
    // The Track attached to the track line
    private Track track;
    // The offset for each file
    private long start;
    // The Track Mixer
    private MixerSetUp mixerSetUp;
    // The part of the track line that displays the waveforms
    private StackPane displayLine;
    // Volume modifier
    private double volume;
    // All files contained in the track line
    private ArrayList<Canvas> audioClips;

    // Pointer shape
    private Rectangle rect;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    private ArrangementWindowController controller;

    private JavaFXController FXController;

    private int index;
    /**
     * Constuctor for the Track Line
     * @param name - Name of Track Line
     * @param control - The parent controller
     */
    public TrackLineGUI(String name, JavaFXController control) {
        lineName = name;
        FXController = control;
        parent = FXController.getSplitPane();
        mainWindow = FXController.getMainWindow();
        mixerSetUp = FXController.getMixerSetUp();
        controller = FXController.getController();

        index = 0;
        audioClips = new ArrayList<>();
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
        trackLine.setPrefHeight(200);

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
        mute.setOnAction(event -> {
            try {
                track.setMute();
                if (track.getMute()) {
                    mute.setTextFill(Color.RED);
                } else {
                    mute.setTextFill(Color.BLACK);
                }
            } catch (NullPointerException e) {
                System.out.println("No track");
            }
        });

        Button solo = new Button("Solo");
        solo.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        solo.setOnAction(event -> {
            try {
                track.setSolo();
                if (track.getSolo()) {
                    solo.setTextFill(Color.RED);
                } else {
                    solo.setTextFill(Color.BLACK);
                }
            } catch (NullPointerException e) {
                System.out.println("No track");
            }
        });

        muteSolo.getChildren().addAll(mute, solo);


        // Delete Channel
        Button deleteChannel = new Button("Delete channel");
        deleteChannel.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        deleteChannel.setOnAction(event -> {
            // Remove the channel from the scene
            parent.getItems().remove(trackLine);
            controller.removeTrack(track);
        });


        volume = 0;
        // volume volumeSlider
        Slider volumeSlider = new Slider(-10, 6, 0);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(4);
        volumeSlider.setMinorTickCount(2);
        volumeSlider.setBlockIncrement(1);
        volumeSlider.setSnapToTicks(true);

        volumeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double newVol = volumeSlider.getValue();
                double diff = newVol - volume;
                double deci = Math.pow(10, (diff/10));
                /*
                System.out.println("The volume went from " + volume + " to " + newVol);
                System.out.println("The difference was " + diff);
                System.out.println("decible change: " + deci);
                */
                adjustVolume((float) deci);
                volume = newVol;
            }
        });

        Label volLabel = new Label();
        volLabel.textProperty().bind(Bindings.format("Volume: %.2f Db", volumeSlider.valueProperty()));

        //Panning
        Slider panSlider = new Slider(-100, 100, 0);
        panSlider.setShowTickLabels(true);
        panSlider.setShowTickMarks(true);
        panSlider.setMajorTickUnit(40);
        panSlider.setMinorTickCount(20);
        panSlider.setBlockIncrement(10);
        panSlider.setSnapToTicks(true);

        Label panLabel = new Label();
        panLabel.textProperty().bind(Bindings.format("Left:%.2f" + " Right: %.2f", panSlider.valueProperty(), panSlider.valueProperty()));


        // Layout for buttons
        optionsBox.getChildren().addAll(name, muteSolo, deleteChannel, volumeSlider, volLabel, panSlider, panLabel);

        //Timeline
        HBox timelineBox = new HBox(75);
        createTimeline(timelineBox);

        // Box that creates a split between timeline and waveform display
        VBox timelineSplit = new VBox();
        displayLine = new StackPane();
        displayLine.setAlignment(Pos.CENTER_LEFT);
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
            System.out.println("No track");
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
                                addFile(file, 0);
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
        int width = (int) Math.round(durationInSeconds*10);
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
            //WaveformEditor w = new WaveformEditor(width, file, durationInSeconds);
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
    public void addFile(File file, long delay) throws Exception {

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        float durationInMilliSeconds = ((frames)/format.getFrameRate()*1000);


        if (audioClips.size() == 0) {
            System.out.println("I tried to add a track");
            track = mixerSetUp.addTrack(file.getName(), file, 1, delay);
            name.textProperty().setValue(file.getName());
        } else {
            System.out.println("I tried to add to a existing track");
            track.addAudioTrackData(file, (start + delay));
        }

        // adjust the volume in case the slider has already been moved
        double volAdjust = Math.pow(10, volume/10);
        adjustVolume((float) volAdjust);


        long position = start + delay;

        WaveformCanvas waveformCanvas = new WaveformCanvas(durationInMilliSeconds, file, index, mixerSetUp, displayLine, position, track);
        Canvas canvas = waveformCanvas.createWaveform();

        start += Math.round(durationInMilliSeconds) + delay;
        System.out.println("Duration is :" + durationInMilliSeconds);
        System.out.println("Next Start at :" + start);

        audioClips.add(canvas);

        displayLine.getChildren().add(canvas);
        index++;
    }

    public HBox createTimeline(HBox box) {

        for (int i = 0; i < 1000; i += 1) {
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

    public int getAudioIndex(Canvas canvas){
        return audioClips.indexOf(canvas);
    }
}
