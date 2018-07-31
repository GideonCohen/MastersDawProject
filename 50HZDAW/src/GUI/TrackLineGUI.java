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

public class TrackLineGUI {
    
    private HBox trackLine;
    private String lineName;
    private SplitPane parent;
    private Scene mainWindow;
    private Track track;
    private long start;
    private MixerSetUp mixerSetUp;
    private HBox displayLine;
    private IntegerProperty volume;

    private ArrayList<File> files;

    // Pointer shape
    private Rectangle rect;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    public TrackLineGUI(String name, SplitPane pane, Scene window, MixerSetUp mixer) {
        lineName = name;
        parent = pane;
        mainWindow = window;
        files = new ArrayList<>();
        mixerSetUp = mixer;
        start = 0;
    }
    
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

        Label name = new Label(lineName);
        
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
        Slider volumeSlider = new Slider();
        volumeSlider.setMin(10);
        volumeSlider.setMax(110);
        volumeSlider.setValue(100);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(20);
        volumeSlider.setMinorTickCount(5);
        volumeSlider.setBlockIncrement(1);

        volume = new SimpleIntegerProperty();
        volume.bind(volumeSlider.valueProperty());
        volume.addListener((v, oldValue, newValue) -> {
            adjustVolume(newValue.floatValue()/oldValue.floatValue());
        });

        // Layout for buttons
        optionsBox.getChildren().addAll(name, muteSolo, gain, deleteChannel, volumeSlider);


        HBox timelineBox = new HBox(75);

        createTimeline(timelineBox);

        // Box that creates a split between timeline and waveform display
        VBox timelineSplit = new VBox();
        displayLine = new HBox(10);
        timelineSplit.getChildren().addAll(timelineBox, displayLine);

        // Final waveform box with pointer, timeline and waveform display
        VBox finalBox = new VBox();

        rect = new Rectangle();
        rect.setStroke(Color.BLACK);
        rect.setWidth(5);
        rect.setHeight(15);
        rect.setFill(Color.BLACK);

        finalBox.getChildren().addAll(rect, timelineSplit);

        trackLine.getChildren().addAll(optionsBox, finalBox);

        acceptDragDrop(trackLine);

        return trackLine;
    }

    private void adjustVolume(float vol) {
        try {
            track.addProcessing(vol);
        } catch (NullPointerException e) {
            //System.out.println("No track);
        }
    }
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
                                Canvas canvas = addFile(file);
                                displayLine.getChildren().add(canvas);
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

    public Canvas createWaveform(Double durationInSeconds, File file) throws Exception {

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
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();
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

    public void addWaveForm(File file) throws java.lang.Exception{
        Canvas canvas = addFile(file);
        displayLine.getChildren().add(canvas);
    }

    public Canvas addFile(File file) throws Exception {

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        double durationInSeconds = (frames+0.0)/format.getFrameRate();

        if (files.size() == 0) {
            System.out.println("I tried to add a track");
            track = mixerSetUp.addTrack(file.getName(), file, (volume.get()/100));
        } else {
            System.out.println("I tried to add to a existing track");
            track.addAudioTrackData(file, 10000);
        }

        Canvas canvas = createWaveform(durationInSeconds, file);

        files.add(file);

        start += file.length();
        System.out.println("Start at :" + start);

        return canvas;
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
