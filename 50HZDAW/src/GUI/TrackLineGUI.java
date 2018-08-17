package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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
    private VBox channels;
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
    // Pan modifier
    private double panLeft;
    private double panRight;
    // All files contained in the track line
    private ArrayList<WaveformCanvas> audioClips;

    // Pointer shape
    private Rectangle rect;

    // The ratio of pixels to milliseconds.
    private double pixelRatio;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    private GUIController controller;

    private FXGUIBuilder FXController;

    private int index;

    private Rectangle trackingLine;

    /**
     * Constuctor for the Track Line
     *
     * @param name    - Name of Track Line
     * @param control - The parent controller
     */
    public TrackLineGUI(String name, FXGUIBuilder control) {
        lineName = name;
        FXController = control;
        channels = FXController.getChannelBox();
        mainWindow = FXController.getMainWindow();
        mixerSetUp = FXController.getMixerSetUp();
        controller = FXController.getController();
        // pixel rate currently hard coded to be 100 pixels
        pixelRatio = FXController.getPixelRatio();

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
        trackLine.getStyleClass().add("track-line");

        // Parent for all settings buttons
        VBox optionsBox = createOptions();

        // Display for the waveforms
        displayLine = new StackPane();
        displayLine.setAlignment(Pos.CENTER_LEFT);
        displayLine.minWidthProperty().bind(FXController.getTimeLine().widthProperty());
        displayLine.getStyleClass().add("grid");

        // Tracking Line
        trackingLine = createTrackingLine();
        displayLine.getChildren().add(trackingLine);

        // Layout
        trackLine.getChildren().addAll(optionsBox, displayLine);

        // Allow for files to be dragged and dropped
        acceptDragDrop(trackLine);

        return trackLine;
    }

    /**
     * Create the options box for the channel line
     * @return VBox options box
     */
    public VBox createOptions() {
        // create options box
        VBox optionsBox = new VBox(5);
        optionsBox.setMinWidth(150);
        optionsBox.setMaxWidth(150);
        optionsBox.getStyleClass().add("options-box");

        // Name label
        name = new Label(lineName);

        // Mute and Solo
        HBox muteSoloDel = new HBox(5);
        Button mute = new Button();
        Image muteImage = new Image("Resources/mute.png");
        Image muteActive = new Image("Resources/muteRed.png");
        mute.setGraphic(new ImageView(muteImage));
        mute.setOnAction(event -> {
            try {
                track.setMute();
                if (track.getMute()) {
                    mute.setGraphic(new ImageView(muteActive));
                } else {
                    mute.setGraphic(new ImageView(muteImage));
                }
            } catch (NullPointerException e) {
                System.out.println("No track");
            }
        });

        Button solo = new Button();
        Image soloImage = new Image("Resources/Solo.png");
        Image soloActive = new Image("Resources/SoloBlue.png");
        solo.setGraphic(new ImageView(soloImage));
        solo.setOnAction(event -> {
            try {
                track.setSolo();
                if (track.getSolo()) {
                    solo.setGraphic(new ImageView(soloActive));
                } else {
                    solo.setGraphic(new ImageView(soloImage));
                }
            } catch (NullPointerException e) {
                System.out.println("No track");
            }
        });

        // Delete Channel
        Button deleteChannel = new Button("");
        Image deleteImage = new Image("Resources/deleteTrack.png");
        deleteChannel.setGraphic(new ImageView(deleteImage));
        deleteChannel.setOnAction(event -> {

            if (ConfirmationBox.Display("Delete Channel", "Are you sure you want to delete this channel?")) {
                // Remove the channel from the scene
                channels.getChildren().remove(trackLine);
                controller.removeTrack(track);
                // remove trackline object from controller
                FXController.getTrackLines().remove(this);
            } else {
                // do nothing
            }

        });

        muteSoloDel.getChildren().addAll(mute, solo, deleteChannel);

        volume = 0;
        // volume volumeSlider
        Slider volumeSlider = new Slider(-18, 6, 0);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(3);
        volumeSlider.setMinorTickCount(2);
        volumeSlider.setBlockIncrement(1);
        volumeSlider.setSnapToTicks(true);

        // Slider listener - adjusts volume
        volumeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double newVol = volumeSlider.getValue();
                double diff = newVol - volume;
                double deci = Math.pow(10, (diff / 10));
                /*
                System.out.println("The volume went from " + volume + " to " + newVol);
                System.out.println("The difference was " + diff);
                System.out.println("decible change: " + deci);
                */
                //System.out.ptinln(deci);
                adjustVolume((float) deci);
                volume = newVol;
            }
        });
        Label volLabel = new Label();
        volLabel.textProperty().bind(Bindings.format("Volume: %.2f Db", volumeSlider.valueProperty()));

        panLeft = 0;
        panRight = 0;
        //Panning
        Slider panSlider = new Slider(-20, 20, 0);
        panSlider.setShowTickMarks(true);
        panSlider.setMajorTickUnit(10);
        panSlider.setMinorTickCount(9);
        panSlider.setBlockIncrement(1);
        panSlider.setSnapToTicks(true);

        Label panLabel = new Label();

        // Pan labels
        DoubleProperty panSliderLeft = new SimpleDoubleProperty();
        panSliderLeft.bind(panSlider.valueProperty().multiply(-1));

        DoubleProperty panSliderRight = new SimpleDoubleProperty();
        panSliderRight.bind(panSlider.valueProperty().multiply(1));

        panLabel.textProperty().bind(Bindings.format("Left:%.0fDb" + " Right: %.0fDb", panSliderLeft, panSliderRight));

        panSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
              //  System.out.println(panSlider.getValue());

                double newLeft = panSlider.getValue() * -1;
                if (newLeft > 0){
                    newLeft = 0;
                }
                double leftDiff =  newLeft - panLeft;
                double leftDeci = Math.pow(10, (leftDiff/ 10));
                //System.out.println("Pan left = " + leftDeci);
                panLeft = newLeft;

                double newRight = panSlider.getValue();
                if (newRight > 0){
                    newRight = 0;
                }
                double rightDiff =  newRight - panRight;
                double rightDeci = Math.pow(10, (rightDiff/ 10));
                //System.out.println("Pan Right = " + rightDeci);
                panRight = newRight;

                track.setPan((float) rightDeci, (float) leftDeci);
            }
        });


        // Layout for buttons
        optionsBox.getChildren().addAll(name, muteSoloDel, volumeSlider, volLabel, panSlider, panLabel);


        // Set button tooltips
        mute.setTooltip(new Tooltip("Mute"));
        solo.setTooltip(new Tooltip("Solo"));
        deleteChannel.setTooltip(new Tooltip("Delete"));
        volumeSlider.setTooltip(new Tooltip("Volume"));
        panSlider.setTooltip(new Tooltip("Pan"));

        return optionsBox;
    }

    /**
     * Adjust the volume of all audio in this track. Values above 1 increase sound, values below decrease sound.
     * Minimum value is 0 max is TBD
     * @param vol - float
     */
    private void adjustVolume(float vol) {
        try {
            track.addVolume(vol);
            //System.out.println(vol);
        } catch (NullPointerException e) {
            System.out.println("No track");
        }
        track.getVolume();
    }

    /**
     * Make an object (in this case an HBox) do accept files via drag and drop
     *
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
     * Add a file to the track and display it
     * @param file
     * @throws Exception
     */
    public void addFile(File file) throws Exception {

        // Get file duration
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        float durationInMilliSeconds = ((frames)/format.getFrameRate()*1000);

        // if there are no clips create a track and add the file
        if (audioClips.size() == 0) {
            //System.out.ptinln("I tried to add a track");
            track = mixerSetUp.addTrack(file.getName(), file, 1, 0);
            name.textProperty().setValue(file.getName());
            lineName = file.getName();
        } else {
            //add the new track
            //System.out.ptinln("I tried to add to a existing track");
            //at the end of the old one
            track.addAudioTrackData(file, (start));
        }

        // adjust the volume in case the slider has already been moved
        double volAdjust = Math.pow(10, volume / 10);
        adjustVolume((float) volAdjust);

        //create the canvas
        WaveformCanvas waveformCanvas = new WaveformCanvas(durationInMilliSeconds, file, index, displayLine, start, track, pixelRatio, this);
        Canvas canvas = waveformCanvas.createWaveform();

        //update the start position
        start += Math.round(durationInMilliSeconds);
        /*
        System.out.println("Duration is :" + durationInMilliSeconds);
        System.out.println("Next Start at :" + start);
        */

        // add new canvas to array
        audioClips.add(waveformCanvas);

        // add canavs to HBox and increase index
        displayLine.getChildren().add(canvas);
        index++;
    }

    /**
     * Resize the canvases of the trackline when the timeline is adjusted
     * @param newPixelRatio
     */
    public void resize(double newPixelRatio) {
        // Update the pixel ration
        pixelRatio = newPixelRatio;
        // Find %change
        double change = newPixelRatio / pixelRatio;
        // for each canvas
        for (WaveformCanvas wfCanvas: audioClips){
            // get the old canvas
            Canvas canvas = wfCanvas.getCanvas();
            // remove it
            displayLine.getChildren().remove(canvas);
            // update the pixel ratio
            wfCanvas.setPixelRatio(pixelRatio);
            // redraw it
            Canvas zoomCanvas = wfCanvas.createWaveform();
            //update the position
            wfCanvas.setPosition((long) (wfCanvas.getPosition()*change));
            // re-add it
            displayLine.getChildren().add(zoomCanvas);
        }

        // Update the tracking line
        displayLine.getChildren().remove(trackingLine);
        trackingLine = createTrackingLine();
        displayLine.getChildren().add(trackingLine);


    }

    /**
     * Create and return a rectangle linked to the position of the pointer for tracking when music is playing
     * @return Rectangle - tracking line
     */
    public Rectangle createTrackingLine() {
        //Create Visale rectangle
        Rectangle trackingLine = new Rectangle();
        //Slightly translucent black
        trackingLine.setStroke(new Color(0, 0, 0, 0.8));
        trackingLine.setWidth(1);
        //Set to the height of the trackline
        trackingLine.setHeight(trackLine.getPrefHeight());
        trackingLine.setFill(Color.BLACK);
        //Bind to the pointer
        trackingLine.translateXProperty().bind(FXController.getPointer().translateXProperty().add(-10));
        return trackingLine;
    }

    /**
     *  Get the audio index of a given canvas within the track line
     * @param wfCanvas
     * @return int audio index
     */
    public int getAudioIndex(WaveformCanvas wfCanvas) {
        return audioClips.indexOf(wfCanvas);
    }

    /**
     *  Get the name of the line
     * @return string line name
     */
    public String getLineName() {
        return lineName;
    }
}
