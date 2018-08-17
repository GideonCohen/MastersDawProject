package GUI;

import Audio.BPMConverter;
import Audio.MixerSetUp;
import Audio.Timing;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;


public class FXGUIBuilder extends Application implements Serializable {

    // Primary Stage
    private Stage window;
    // Primary Scene
    private Scene mainWindow;
    // Split pane for the main window
    private VBox channels;
    // pointer for the timeline
    private Rectangle pointer;
    // Wav file directory
    private DirectoryViewer directory;
    // Visual representation of the timeline
    private VBox timeLine;

    // Controller for handling user events
    private GUIController controller;
    // Audio Player
    private MixerSetUp mixerSetUp;
    // TrackLine List
    private ArrayList<TrackLineGUI> trackLines;
    // The ration of pixels to milliseconds. e.g. a ratio of 0.1 means 1 pixel = 10 milliseconds
    private double pixelRatio;
    // The ration of pixels to milliseconds. e.g. a ratio of 0.1 means 1 pixel = 10 milliseconds
    private double timelineRatio;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;
    private float locatorRatio;
    // Timer
    private Timing timing;
    // Timer label
    private Label timer;
    // Beats and bars label
    private Label beatsAndBarsLabel;
    // Convert pixels to millis to beats & bars
    private BPMConverter bpmConverter;
    // length of a bar at given BPM.
    private double barLength;

    public static void main(String[] args) {
        // calls the args method of application
        // must override start method
        launch(args);
    }

    /**
     * Start method from JavaFX application
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // The mixer for the audio
        mixerSetUp = new MixerSetUp(0);

        // Initial pixel ratio, default is 100 pixels per second.

        // Empty track line array
        trackLines = new ArrayList<>();

        // Controller for the Gui
        controller = new GUIController(this, mixerSetUp);

        // Converter for milliseconds to Beats and bars
        bpmConverter = new BPMConverter();
        barLength = (bpmConverter.setBars(1, mixerSetUp.getBpm()))/(double)1000;
        System.out.println(barLength);
        pixelRatio = 0.1/ barLength;
        timelineRatio = 1;
        locatorRatio = 1;

        // Directory for storing samples
        directory = new DirectoryViewer(this);

        // Create the main layout
        makeMainWindow();

        // Set window as primary stage and give it a title
        window = primaryStage;
        primaryStage.setTitle("JavaFX DAW");

        // If you close with the X runs a method
        window.setOnCloseRequest(event -> {
            // cancel the event, my app will take care of it.
            event.consume();
            controller.closeProgram();
        });

        //Set starting scene and show
        primaryStage.setScene(mainWindow);
        primaryStage.show();

    }

    /**
     * The Main Layout of the application (Essentially everything except the menu bar)
     */
    private void makeMainWindow(){
        //Splits the menu and the rest of the window
        VBox mainSplit = new VBox(0);

        // Main layout for the window
        BorderPane mainLayout = new BorderPane();

        // Add the split pane to a scroll pane so able to show many windows
        ScrollPane channelView = new ScrollPane();
        StackPane backPane = new StackPane();
        backPane.setId("back-pane");

        // add arrangement layout
        channels = new VBox();
        channels.setMinHeight(1);

        // Bind the channels to be as wide as the alignment window
        channels.minWidthProperty().bind(channelView.widthProperty());
        timeLine = createTimeline();
        channels.getChildren().add(timeLine);

        // Import and load background
        String localUrl = "";

        timing = new Timing ();

        // Find the image location for the background
        try {
            // image location
            File file = new File("50HZDAW/src/Resources/WoodGrain.jpeg");
            localUrl = file.toURI().toURL().toString();
        } catch (Exception e) {
        }

        Image backImage = new Image("Resources/WoodGrain.jpeg", false);
        // size = change cover to true for repeating images
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, false, false, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(backImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        Background background = new Background(backgroundImage);

        //Set background of scroll pane to show image, uncomment to add
        channelView.setBackground(background);
        channelView.setContent(channels);

        // Add split for the directory
        HBox directorySplit = new HBox(0);
        StackPane directoryGUI = directory.makeDirectory();
        directorySplit.getChildren().addAll(directoryGUI, channelView);

        // Set positions in the layout
        mainLayout.setTop(makeTopLine());
        mainLayout.setCenter(directorySplit);

        // add children to split
        mainSplit.getChildren().add(makeMenu());
        mainSplit.getChildren().add(mainLayout);

        // make the scene
        mainWindow = new Scene(mainSplit, 1200, 600);
        mainWindow.getStylesheets().add("GUI/Style.css");


        // Allow for drag and drop for adding files
        dragDropTracks(channelView);

        // Bind the arrangemnet window to be just smaller than the app window
        channelView.prefHeightProperty().bind(mainWindow.heightProperty());

    }

    /**
     * Create a Menu Bar for the application
     *
     * @return MenuBar
     */
    private MenuBar makeMenu(){

        // file menu
        Menu fileMenu = new Menu("File");

        // File Items
        MenuItem addTrack = new MenuItem("Add Audio");
        // Import file via browser window
        addTrack.setOnAction(event -> {
            importFile();
        });
        fileMenu.getItems().add(addTrack);

        // Add a new empty track
        MenuItem newTrack = new MenuItem("New Track");
        newTrack.setOnAction(event -> {
            TrackLineGUI trackLine = new TrackLineGUI("New Track", this);
            channels.getChildren().add(trackLine.createTrack());
            trackLines.add(trackLine);
        });
        fileMenu.getItems().add(newTrack);

        // Export the current arrangement as a wav file
        MenuItem exportAsWav = new MenuItem("Export project as WAV");
        exportAsWav.setOnAction(event -> export());
        fileMenu.getItems().add(exportAsWav);

        // Aesthetic seperator
        fileMenu.getItems().add(new SeparatorMenuItem());

        //Close Application
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> {
            controller.closeProgram();
        });
        fileMenu.getItems().add(exit);

        // About Menu
        Menu aboutMenu = new Menu("About");

        // ABout Menu Items
        MenuItem aboutUs = new MenuItem("About");
        aboutMenu.getItems().add(aboutUs);

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, aboutMenu);
        return menuBar;

    }

    /**
     * The top line with most of the basic player buttons and a timer
     * @return - HBox with basic player buttons and a timer
     */
    private HBox makeTopLine() {

        // The top line
        HBox topLine = new HBox(20);


        // Media Player button block
        HBox playerButtons = new HBox(10);
        playerButtons.setAlignment(Pos.CENTER);

        Region r = new Region();

        // Play all added tracks
        Button play = new Button();
        Image playImage = new Image("Resources/play.png");
        play.setGraphic(new ImageView(playImage));
        play.setOnAction(event -> {
            // play the arrangement
            System.out.println("TRACK POS: " + timing.getMillis());
            controller.play((int)timing.getMillis());
            // start the timer
            timing.getTimerMillis(mixerSetUp.getBpm(), timer, beatsAndBarsLabel);   // timer adapts to bpm change (bars & beats calculated)
            timing.startTimer();
        });

        // Pause all added tracks
        Button pause = new Button();
        Image pauseImage = new Image("Resources/pause.png");
        pause.setGraphic(new ImageView(pauseImage));
        pause.setOnAction(event -> {
                // pause the timer
                timing.pauseTimer();
                System.out.println("TRACK POS: " + timing.getMillis());
                // pause the arrangement
                controller.pause((int)timing.getMillis());
        });

        // Stop all added tracks
        Button stop = new Button();
        Image stopImage = new Image("Resources/stop.png");
        stop.setGraphic(new ImageView(stopImage));
        stop.setOnAction(event -> {
                 // stop the arrangement
                controller.stop((int)timing.getMillis());
                // stop and reset the timer
                timing.stopTimer();
                TT.stop();
                pointer.setTranslateX(10);
            System.out.println("TRACK POS: " + timing.getMillis());
            timer.setText("00:00");
    });

        Button zoomIn = new Button("Zoom In");
        zoomIn.setOnAction(event -> {
            setPixelRatio((pixelRatio * 2), (timelineRatio / 2));
            if(locatorRatio > 1) {
                locatorRatio = locatorRatio * 2;
            }
            else {
                locatorRatio = 1; 
            }
            TT.setToX((1000 * 10) * locatorRatio);

        });

        Button zoomOut = new Button("Zoom Out");
        zoomOut.setOnAction(event -> {
            setPixelRatio((pixelRatio / 2), timelineRatio * 2);
            locatorRatio = locatorRatio / 2;
            TT.setToX(((1000 * 10) * locatorRatio) + 10);
        });

        // add tootips
        play.setTooltip(new Tooltip("Play"));
        pause.setTooltip(new Tooltip("Pause"));
        stop.setTooltip(new Tooltip("Stop"));
        zoomIn.setTooltip(new Tooltip("Zoom In"));
        zoomOut.setTooltip(new Tooltip("Zoom Out"));

        Button metronomeButton = new Button("Metronome");
        metronomeButton.setOnAction(event -> {
            //timing.setMetronome();
        });

        playerButtons.getChildren().addAll(r, play, pause, stop, zoomIn, zoomOut, metronomeButton);


        // Timer
        HBox timerLine = new HBox(20);

        timer = new Label();
        timer.setText("00:00");
        timer.setFont(new Font(34));
        timer.setMinWidth(100);

        timerLine.getChildren().add(timer);

        // Bar display
        HBox bpmBox = new HBox(50);

        beatsAndBarsLabel = new Label();
        Label bpmLabel = new Label();
        beatsAndBarsLabel.setText("bars: " + "1  " + "beats: " + "1");
        beatsAndBarsLabel.setFont(new Font(24));
        bpmLabel.setText(mixerSetUp.getBpm() +  " bpm");
        bpmLabel.setFont(new Font(24));

        // BPM selector
        ComboBox bpmSelect = new ComboBox();
        int oneHundred = 100;
        int oneTen = 110;
        int oneTwenty = 120;
        int oneTwentyFive = 125;
        int oneTwentySix = 126;
        int oneTwentyEight = 128;
        int oneThirty = 130;
        int oneFourty = 140;
        int oneSeventy = 170;
        bpmSelect.setPromptText("120");


        bpmSelect.getItems().addAll(oneHundred, oneTen, oneTwenty, oneTwentyFive, oneTwentySix, oneTwentyEight, oneThirty, oneFourty, oneSeventy);

        bpmBox.getChildren().add(beatsAndBarsLabel);
        bpmBox.getChildren().add(bpmLabel);

        bpmBox.getChildren().add(bpmSelect);

        bpmSelect.setOnAction(doThis -> {
            System.out.println(bpmSelect.getItems().get(bpmSelect.getSelectionModel().getSelectedIndex()).hashCode());
            mixerSetUp.setBpm((bpmSelect.getItems().get(bpmSelect.getSelectionModel().getSelectedIndex()).hashCode()));
            System.out.println(mixerSetUp.getBpm());
            bpmLabel.setText(mixerSetUp.getBpm() +  " bpm");
            setPixelBpmChange();

        });

        beatsAndBarsLabel.setMinWidth(150);
        // beatsAndBarsLabel.setPrefSize(beatsAndBarsLabel.USE_PREF_SIZE, beatsAndBarsLabel.USE_PREF_SIZE);

        bpmLabel.setMinWidth(150);
        // bpmLabel.setPrefSize(bpmLabel.USE_PREF_SIZE, bpmLabel.USE_PREF_SIZE);

        // App Button Line

        topLine.getChildren().addAll(playerButtons, timerLine, bpmBox);

        return topLine;
    }

    /**
     * Allows .wav files to be added to the application. Will create channels as files are added
     */
    private void dragDropTracks(ScrollPane scroll){

        // Handler for drag over
        scroll.setOnDragOver(new EventHandler<DragEvent>() {
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
        scroll.setOnDragDropped(new EventHandler<DragEvent>() {
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
     * Import a file from the Menu Bar
     */
    public void importFile(){
        // Import file
        ImportManager importManager = new ImportManager();
        // Create trackline
        TrackLineGUI trackline = importManager.importFile(channels, this, window);
        // Add to list of tracklines
        trackLines.add(trackline);
    }

    /**
     * Export the current arrangement as a 24 bit .wav
     */
    public void export() {
        try {
            // Export the current arrangement
            controller.export(window);
        } catch (LineUnavailableException e) {
            // do something
        }
    }

    /**
     * Create the timeline
     * @return VBox with timeline and pointer
     */
    public VBox createTimeline() {

        // Convert pixel ratio to seconds
        double pixelsPerSec = (1/(pixelRatio * 10));

        // Split between timer and pointer
        VBox timeSplit = new VBox(0);
        HBox timeBox = new HBox(50);
        for (double i = 1; i < 500; i += timelineRatio) {
            Label label = new Label();
            // bind to current valye to 2dp
            label.textProperty().bind(Bindings.format("%.2f", i ));
            label.setMinWidth(50);
            label.setMinHeight(25);
            // width + padding should always = 100
            label.setMaxWidth(50);
            label.setTextAlignment(TextAlignment.LEFT);
            // add label to HBox
            timeBox.getChildren().add(label);
        }

        // Set staring position
        timeSplit.setTranslateX(165);
        //System.out.println(pixelsPerSec);
        // Add the pointer
        createPointer();
        timeSplit.getChildren().addAll(timeBox, pointer);
        return timeSplit;
    }

    /**
     * Create pointer for tracking current position
     * @return Rectangle pointer
     */
    public Rectangle createPointer() {

        // Create pointer
        pointer = new Rectangle();
        pointer.setStroke(Color.BLACK);
        pointer.setWidth(5);
        pointer.setHeight(5);
        pointer.setFill(Color.BLACK);
        pointer.setTranslateX(10);

        // Set tranlation speed
        double pointerSpeed = 100 * barLength;    // multiple value to make pointer go slower.

        // Set animation
        TT = new TranslateTransition(Duration.seconds(pointerSpeed), pointer);
        TT.setToX((1000 * 10) * locatorRatio);
        TT.setInterpolator(Interpolator.LINEAR);
        mixerSetUp.setTT(TT);
        mixerSetUp.setRectangle(pointer);

        return pointer;
    }

    public void setPixelBpmChange () {

        barLength = (bpmConverter.setBars(1, mixerSetUp.getBpm()))/(double)1000;
        pixelRatio = 0.1/ barLength;
        timelineRatio = 1;
        setPixelRatio(pixelRatio, timelineRatio);

    }

    /**
     * Change the pixel ratio and adjust
     * @param pixelRatio - new pixel to millisecond ratio
     * @param timelineRatio - new pixel to millisecond ratio adjusted for timeline
     */
    public void setPixelRatio(double pixelRatio, double timelineRatio) {

        try {
            // 0.003 around about 32seconds per 100 pixels      // translated to a zoom in of 32 bars at a time. (NOW 16!)
            if (pixelRatio <= 0.1 && pixelRatio > 0.003) {        //  (NOW 16!)
                System.out.println(pixelRatio);
                this.pixelRatio = pixelRatio;
                this.timelineRatio = timelineRatio;

                channels.getChildren().remove(0);
                timeLine = createTimeline();
                channels.getChildren().add(0, timeLine);

                for (TrackLineGUI trackline : trackLines) {
                    //System.out.println(trackline.getLineName());
                    trackline.resize(this.pixelRatio);
                }

            } else {
                System.out.println("Pixel ratio mus be between 0 and 1");
            }
        } catch(RuntimeException e) {
            System.out.println("Error canvas too large");
        }


    }

    /**
     * Create a new trackline with a specific file
     * @param file - .wav file to add
     * @throws java.lang.Exception
     */
    public void addFile(File file) throws java.lang.Exception{
        TrackLineGUI trackLine = new TrackLineGUI(file.getName(), getThis());
        channels.getChildren().add(trackLine.createTrack());
        trackLine.addFile(file);
        trackLines.add(trackLine);
    }

    /**
     * Get the stage
     * @return stage window
     */
    public Stage getWindow() {
        return window;
    }

    /**
     * get the pointer animation
     * @return TranslateTransition TT
     */
    public TranslateTransition getTT() {
        return TT;
    }

    /**
     * Get the timer controller
     * @return timing
     */
    public Timing getTiming() {
        return timing;
    }

    /**
     * Get the arraylist of all track lines
     * @return ArrayList<TrackLineGUI>
     */
    public ArrayList<TrackLineGUI> getTrackLines() {
        return trackLines;
    }

    /**
     * Get the timeline display
     * @return VBox timeline
     */
    public VBox getTimeLine() {
        return timeLine;
    }

    /**
     * Get the tracking line pointer
     * @return Rectangle pointer
     */
    public Rectangle getPointer() {
        return pointer;
    }

    /**
     * get the pixel to millisecond ration
     * @return Double pixel ratio
     */
    public double getPixelRatio() {
        return pixelRatio;
    }

    /**
     * Return the gui controller
     * @return - GUIController
     */
    public GUIController getController() {
        return controller;
    }

    /**
     * Get the mixer
     * @return MixerSetUp mixerSetUp
     */
    public MixerSetUp getMixerSetUp() {
        return mixerSetUp;
    }

    /**
     * Returns the main scene
     * @return Scene mainWindow
     */
    public Scene getMainWindow() {
        return mainWindow;
    }

    /**
     * Returns this FXBuilder
     * @return FXGUIBuilder this
     */
    public FXGUIBuilder getThis () {
        return this;
    }

    /**
     * Return the parent for all the channel lines
     * @return VBox channels
     */
    public VBox getChannelBox() {
        return channels;
    }

    public void killTimer () {
        if(mixerSetUp.getTimerStatus() == false) {
            timing.stopTimer();
        }
    }
}
