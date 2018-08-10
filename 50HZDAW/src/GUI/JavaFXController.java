package GUI;

import Audio.BPMConverter;
import Audio.MixerSetUp;
import Audio.Timing;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;


public class JavaFXController extends Application implements Serializable {

    // Primary Stage
    private Stage window;

    // Primary Scene
    private Scene mainWindow;

    // Controller for handling user events
    private ArrangementWindowController controller;

    // Audio Player
    private MixerSetUp mixerSetUp;

    // Split pane for the main window
    private VBox channels;

    // TrackLine List
    private ArrayList<TrackLineGUI> trackLines;

    // pointer for the timeline
    private Rectangle pointer;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    // The ration of pixels to milliseconds. e.g. a ratio of 0.1 means 1 pixel = 10 milliseconds
    private double pixelRatio;
    // The ration of pixels to milliseconds. e.g. a ratio of 0.1 means 1 pixel = 10 milliseconds
    private double timelineRatio;

    private VBox timeLine;

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

    private DirectoryViewer directory;





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
        controller = new ArrangementWindowController(this, mixerSetUp);

        bpmConverter = new BPMConverter();
        barLength = (bpmConverter.setBars(1, mixerSetUp.getBpm()))/(double)1000;
        System.out.println(barLength);


        pixelRatio = 0.1/ barLength;
        timelineRatio = 1;

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


        try {
            // image location
            File file = new File("50HZDAW/Samples/WoodGrain.jpeg");
            localUrl = file.toURI().toURL().toString();
        } catch (Exception e) {
        }

        Image backImage = new Image(localUrl, false);
        // size = change cover to true for repeating images
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, false, false, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(backImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        Background background = new Background(backgroundImage);

        //Set background of scroll pane to show image, uncomment to add
        channelView.setBackground(background);
        channelView.setContent(channels);

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

        MenuItem newTrack = new MenuItem("New Track");
        newTrack.setOnAction(event -> {
            TrackLineGUI trackLine = new TrackLineGUI("New Track", this);
            channels.getChildren().add(trackLine.createTrack());
            trackLines.add(trackLine);
        });
        fileMenu.getItems().add(newTrack);

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

        // Edit Menu
        Menu editMenu = new Menu("Edit");

        // Edit Items
        MenuItem cut = new MenuItem("Cut");
        cut.setOnAction(event -> {
            System.out.println("Cut selected");
        });
        editMenu.getItems().add(cut);

        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(event -> {
            System.out.println("Copy selected");
        });
        editMenu.getItems().add(copy);

        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(event -> {
            System.out.println("Paste selected");
        });
        editMenu.getItems().add(paste);

        // Layout Menu
        Menu layoutMenu = new Menu("Layout");

        // Layout Menu Items
        MenuItem changeLayout = new MenuItem("Change Layout");
        layoutMenu.getItems().add(changeLayout);

        // About Menu
        Menu aboutMenu = new Menu("About");

        // ABout Menu Items
        MenuItem aboutUs = new MenuItem("About");
        aboutMenu.getItems().add(aboutUs);


        // Menu Bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, editMenu, layoutMenu, aboutMenu);
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
            controller.play();
            timing.getTimerMillis(mixerSetUp.getBpm(), timer, beatsAndBarsLabel);   // timer adapts to bpm change (bars & beats calculated)
            timing.startTimer();
        });


        // Pause all added tracks
        Button pause = new Button();
        Image pauseImage = new Image("Resources/pause.png");
        pause.setGraphic(new ImageView(pauseImage));
        pause.setOnAction(event -> {
                timing.pauseTimer();
                controller.pause();
    });


        // Stop all added tracks
        Button stop = new Button();
        Image stopImage = new Image("Resources/stop.png");
        stop.setGraphic(new ImageView(stopImage));
        stop.setOnAction(event -> {
                controller.stop();
                timing.stopTimer();
                TT.stop();
                pointer.setTranslateX(0);
                timer.setText("00:00");
    });

        Button zoomIn = new Button("Zoom In");
        zoomIn.setOnAction(event -> setPixelRatio((pixelRatio*2), (timelineRatio/2)));

        Button zoomOut = new Button("Zoom Out");
        zoomOut.setOnAction(event -> setPixelRatio((pixelRatio/2), timelineRatio*2));

        playerButtons.getChildren().addAll(r, play, pause, stop, zoomIn, zoomOut);


        // Timer
        HBox timerLine = new HBox(20);

        timer = new Label();
        timer.setText("00:00");
        timer.setFont(new Font(34));
        timer.setMinWidth(100);



        timerLine.getChildren().add(timer);

        HBox bpmBox = new HBox(50);

        beatsAndBarsLabel = new Label();
        Label bpmLabel = new Label();
        beatsAndBarsLabel.setText("bars: " + "1  " + "beats: " + "1");
        beatsAndBarsLabel.setFont(new Font(24));
        bpmLabel.setText(mixerSetUp.getBpm() +  " bpm");
        bpmLabel.setFont(new Font(24));

        ComboBox bpmSelect = new ComboBox();
        int oneTwenty = 120;
        int oneThirty = 130;
        int oneFourty = 140;
        bpmSelect.setPromptText("120");

        bpmSelect.getItems().addAll(oneTwenty, oneThirty, oneFourty);

        bpmBox.getChildren().add(beatsAndBarsLabel);
        bpmBox.getChildren().add(bpmLabel);

        bpmBox.getChildren().add(bpmSelect);

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
                                TrackLineGUI trackLine = new TrackLineGUI(file.getName(), getThis());
                                channels.getChildren().add(trackLine.createTrack());
                                trackLine.addFile(file);
                                trackLines.add(trackLine);
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
        ImportManager importManager = new ImportManager();
        TrackLineGUI trackline = importManager.importFile(channels, this, window);
        trackLines.add(trackline);
    }

    public void export() {
        try {
            controller.export(window);
        } catch (LineUnavailableException e) {
            // do something
        }
    }

    public Stage getWindow() {
        return window;
    }

    /**
     * Return the gui controller
     * @return - ArrangementWindowController
     */
    public ArrangementWindowController getController() {
        return controller;
    }

    public MixerSetUp getMixerSetUp() {
        return mixerSetUp;
    }

    public Scene getMainWindow() {
        return mainWindow;
    }

    public JavaFXController getThis () {
        return this;
    }

    public VBox getVBox() {
        return channels;
    }

    public VBox createTimeline() {


        double pixelsPerSec = (1/(pixelRatio * 10));

        VBox timeSplit = new VBox(0);
        HBox timeBox = new HBox(50);
        for (double i = 1; i < 500; i += timelineRatio) {

            Label label = new Label();

            label.textProperty().bind(Bindings.format("%.2f", i ));
            label.setMinWidth(50);
            label.setMinHeight(25);
            label.setMaxWidth(50);
            label.setTextAlignment(TextAlignment.LEFT);

            timeBox.getChildren().add(label);
        }

        timeSplit.setTranslateX(165);

        System.out.println(pixelsPerSec);
        createPointer();
        timeSplit.getChildren().addAll(timeBox, pointer);
        return timeSplit;
    }

    public Rectangle createPointer() {

        pointer = new Rectangle();
        pointer.setStroke(Color.BLACK);
        pointer.setWidth(5);
        pointer.setHeight(5);
        pointer.setFill(Color.BLACK);
        pointer.setTranslateX(5);

        double pointerSpeed = 100 * barLength;    // multiple value to make pointer go slower.

        TT = new TranslateTransition(Duration.seconds(pointerSpeed), pointer);
        TT.setToX(1000 * 10);
        TT.setInterpolator(Interpolator.LINEAR);

        mixerSetUp.setTT(TT);

        return pointer;
        
        /*
        TT = new TranslateTransition(Duration.seconds(width), pointer);
        TT.setToX(width * 10);
        TT.setInterpolator(Interpolator.LINEAR);
        */
    }

    public Rectangle getPointer() {
        return pointer;
    }

    public double getPixelRatio() {
        return pixelRatio;
    }

    public void setPixelRatio(double pixelRatio, double timelineRatio) {

        try {
            // 0.003 around about 32seconds per 100 pixels      // translated to a zoom in of 32 bars at a time. (NOW 16!)
            if (pixelRatio <= 0.1 && pixelRatio > 0.003) {        //  (NOW 16!)
                System.out.println(pixelRatio);
                this.pixelRatio = pixelRatio;
                this.timelineRatio = timelineRatio;

                for (TrackLineGUI trackline : trackLines) {
                    //System.out.println(trackline.getLineName());
                    trackline.resize(this.pixelRatio);
                }
                channels.getChildren().remove(0);
                timeLine = createTimeline();
                channels.getChildren().add(0, timeLine);
            } else {
                System.out.println("Pixel ratio mus be between 0 and 1");
            }
        } catch(RuntimeException e) {
            System.out.println("Error canvas too large");
        }


    }

    public ArrayList<TrackLineGUI> getTrackLines() {
        return trackLines;
    }

    public VBox getTimeLine() {
        return timeLine;
    }

    public void setMainScene() {
        window.setScene(mainWindow);
    }
}
