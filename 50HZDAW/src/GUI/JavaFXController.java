package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.Serializable;


public class JavaFXController extends Application implements Serializable {

    // Primary Stage
    private Stage window;

    // Primary Scene
    private Scene mainWindow;

    // Pointer shape
    private Rectangle rect;

    // TranslateTransition object used to move pointer over time
    private TranslateTransition TT;

    // Controller for handling user events
    private ArrangementWindowController controller;

    // Audio Player
    private MixerSetUp mixerSetUp;

    private boolean isPointerShowing = false;




/*    public static void main(String[] args) {
        // calls the args method of application
        // must override start method
        launch(args);
    }*/

    /**
     * Start method from JavaFX application
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {



        // Create the main layout
        makeMainWindow();

        mixerSetUp = new MixerSetUp(0);

        controller = new ArrangementWindowController(this, mixerSetUp);

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
        VBox split1 = new VBox(0);

        // Main layout for the window
        BorderPane mainLayout = new BorderPane();



        // Add the split pane to a scroll pane so able to show many windows
        ScrollPane channels = new ScrollPane();

        // add arrangement layout
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setMinSize(1,1);

        // Bind the channels to be as wide as the alignment window
        splitPane.minWidthProperty().bind(channels.widthProperty());

        channels.setContent(splitPane);



        // Set positions in the layout
        mainLayout.setTop(makeTopLine());
        mainLayout.setCenter(channels);

        // add children to split
        split1.getChildren().add(makeMenu(splitPane));
        split1.getChildren().add(mainLayout);

        // make the scene
        mainWindow = new Scene(split1, 800, 800);



        // Allow for drag and drop for adding files
        dragDropTracks(mainWindow, splitPane, channels);

        // Bind the arrangemnet window to be just smaller than the app window
        channels.prefHeightProperty().bind(mainWindow.heightProperty().add(-100));

    }

    /**
     * Create a Menu Bar for the application
     *
     * @param pane - Split pane for adding channels
     * @return MenuBar
     */
    private MenuBar makeMenu(SplitPane pane){

        // file menu
        Menu fileMenu = new Menu("File");

        // File Items
        MenuItem addTrack = new MenuItem("Add Track");
        // Import file via browser window
        addTrack.setOnAction(event -> {
            importFile(pane);
        });
        fileMenu.getItems().add(addTrack);

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
    private HBox makeTopLine(){

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
        play.setOnAction(event -> controller.play());


        // Pause all added tracks
        Button pause = new Button();
        Image pauseImage = new Image("Resources/pause.png");
        pause.setGraphic(new ImageView(pauseImage));
        pause.setOnAction(event -> controller.pause());


        // Stop all added tracks
        Button stop = new Button();
        Image stopImage = new Image("Resources/stop.png");
        stop.setGraphic(new ImageView(stopImage));
        stop.setOnAction(event -> controller.stop());


        // Currently non functional
        Button skipF = new Button();
        Image FFImage = new Image("Resources/ff.png");
        skipF.setGraphic(new ImageView(FFImage));

        Button skipB = new Button();
        Image RWImage = new Image("Resources/rw.png");
        skipB.setGraphic(new ImageView(RWImage));

        playerButtons.getChildren().addAll(r, play, pause, stop, skipB, skipF);


        // Timer
        HBox timerLine = new HBox();

        Label timer = new Label("00:00:00");
        timer.setFont(new Font(34));
        timerLine.getChildren().add(timer);

        // App Button Line

        topLine.getChildren().addAll(playerButtons, timerLine);

        return topLine;

    }


    /**
     * Make a channel line when a new file is added
     * @param file - the .wav file attatched to the channel line
     * @param pane - the SplitPane that houses all the channels
     */
    private void makeChannelLine(File file, SplitPane pane) throws Exception{



        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        double durationInSeconds = (frames+0.0)/format.getFrameRate();
        System.out.println("Audio file is " + durationInSeconds + " Seconds long");

        Track track = mixerSetUp.addTrack(file.getName(), file, 1f);

        // Whole Channel - Settings and waveform
        HBox channelBox = new HBox(30);
        channelBox.prefWidthProperty().bind(mainWindow.widthProperty());

        // Parent for all settings buttons
        VBox optionsBox = new VBox(5);
        optionsBox.setMinHeight(1);
        optionsBox.setMinWidth(100);
        optionsBox.setMaxWidth(100);


        // Channel Name
        Label fileName = new Label(file.getName());
        fileName.getStyleClass().add("track-label");

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
            pane.getItems().remove(channelBox);
            controller.removeTrack(track);
        });

        // Layout for buttons
        optionsBox.getChildren().addAll(fileName, muteSolo, gain, deleteChannel);


        HBox timelineBox = new HBox(75);

        createTimeline(timelineBox);

        // Box that creates a split between timeline and waveform display
        VBox timelineSplit = new VBox();
        WaveformCanvas WC = new WaveformCanvas(durationInSeconds, file);
        timelineSplit.getChildren().addAll(timelineBox, WC.createWaveform());

        // Final waveform box with pointer, timeline and waveform display
        VBox finalBox = new VBox();

        int width = (int) Math.round(durationInSeconds);

        if (!isPointerShowing) {
            createPointer(width);
            finalBox.getChildren().add(rect);
            isPointerShowing = true;
        }

        finalBox.getChildren().add(timelineSplit);

        channelBox.getChildren().addAll(optionsBox, finalBox);
        pane.getItems().add(channelBox);

    }

    /**
     * Allows .wav files to be added to the application. Will create channels as files are added
     * @param scene - Area that can be dragged into
     * @param pane - SplitPane to add the channels to
     */
    private void dragDropTracks(Scene scene, SplitPane pane, ScrollPane scroll){

        // Handler for drag over
        scroll.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
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
                // if item is dropped
                if (db.hasFiles()) {
                    success = true;
                    for (File file:db.getFiles()) {
                        if (file.getName().endsWith(".wav")) {
                            try {
                                makeChannelLine(file, pane);
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
     * @param pane - the pane to add the channel too
     */
    public void importFile(SplitPane pane){
        // JavaFX prebuilt file chooser
        FileChooser fileChooser = new FileChooser();

        // Create and add filter for only .wav files
        FileChooser.ExtensionFilter wavFilter =
                new FileChooser.ExtensionFilter("Wav Files (*.wav)", "*.wav");
        fileChooser.getExtensionFilters().add(wavFilter);

        fileChooser.setTitle("Choose a Track");
        File file = fileChooser.showOpenDialog(window);

        // If appropriate file type is chosen
        try {
            // Make a channel for the player
            makeChannelLine(file, pane);
        } catch(Exception e) {

        }
    }


    public Stage getWindow() {
        return window;
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

    public TranslateTransition getTT() {
        return TT;
    }

    public Rectangle getRect() {
        return rect;
    }



}
