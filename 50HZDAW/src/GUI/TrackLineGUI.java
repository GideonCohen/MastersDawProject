package GUI;

import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;

public class TrackLineGUI {
    
    private HBox trackLine;
    private String lineName;
    private SplitPane parent;
    private Scene mainWindow;

    private ArrayList<File> files;

    // Pointer shape
    private Rectangle rect;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    public TrackLineGUI(String name, SplitPane pane, Scene window) {
        lineName = name;
        parent = pane;
        mainWindow = window;
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

        // Layout for buttons
        optionsBox.getChildren().addAll(name, muteSolo, gain, deleteChannel);


        HBox timelineBox = new HBox(75);

        createTimeline(timelineBox);

        // Box that creates a split between timeline and waveform display
        VBox timelineSplit = new VBox();
        timelineSplit.getChildren().addAll(timelineBox, new Canvas(100, 100));

        // Final waveform box with pointer, timeline and waveform display
        VBox finalBox = new VBox();

        rect = new Rectangle();
        rect.setStroke(Color.BLACK);
        rect.setWidth(5);
        rect.setHeight(15);
        rect.setFill(Color.BLACK);

        finalBox.getChildren().addAll(rect, timelineSplit);

        trackLine.getChildren().addAll(optionsBox, finalBox);
        parent.getItems().add(trackLine);

        acceptDragDrop(trackLine);

        return trackLine;
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

    public void addWaveForm() {
    }

    public void addFile(File file) {
        System.out.println("I tried to add a file");
        files.add(file);
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
