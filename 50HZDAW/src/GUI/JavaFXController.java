package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
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

    // Controller for handling user events
    private ArrangementWindowController controller;

    // Audio Player
    private MixerSetUp mixerSetUp;

    // Split pain for the main window
    private SplitPane splitPane;

    private ImageView background;

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

        // Create the main layout
        makeMainWindow();

        // The mixer for the audio
        mixerSetUp = new MixerSetUp(0);

        // Controller for the Gui
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
        VBox mainSplit = new VBox(0);

        // Main layout for the window
        BorderPane mainLayout = new BorderPane();

        // Add the split pane to a scroll pane so able to show many windows
        ScrollPane channels = new ScrollPane();
        StackPane backPane = new StackPane();
        backPane.setId("back-pane");

        // add arrangement layout
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setMinSize(1,1);

        // Bind the channels to be as wide as the alignment window
        splitPane.minWidthProperty().bind(channels.widthProperty());

        channels.setContent(backPane);



        try {
        File file = new File("50HZDAW/Samples/TestBackground.jpg");
        String localUrl = file.toURI().toURL().toString();
        // don't load in the background
        Image backgroundImage = new Image(localUrl, false);

        this.background = new ImageView();
        background.setImage(backgroundImage);
        background.setCache(true);
        background.fitHeightProperty();
        } catch (Exception e) {
        }

        backPane.getChildren().addAll(background, splitPane);

        // Set positions in the layout
        mainLayout.setTop(makeTopLine());
        mainLayout.setCenter(channels);

        // add children to split
        mainSplit.getChildren().add(makeMenu(splitPane));
        mainSplit.getChildren().add(mainLayout);

        // make the scene
        mainWindow = new Scene(mainSplit, 1200, 600);
        mainWindow.getStylesheets().add("GUI/Style.css");


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
        MenuItem addTrack = new MenuItem("Add Audio");
        // Import file via browser window
        addTrack.setOnAction(event -> {
            importFile(pane);
        });
        fileMenu.getItems().add(addTrack);

        MenuItem newTrack = new MenuItem("New Track");
        newTrack.setOnAction(event -> {
            TrackLineGUI trackLine = new TrackLineGUI("New Track", this);
            pane.getItems().add(trackLine.createTrack());
        });
        fileMenu.getItems().add(newTrack);

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
        HBox playerButtons = new HBox();

        // Play all added tracks
        Button play = new Button("play");
        play.setOnAction(event -> controller.play());


        // Pause all added tracks
        Button pause = new Button("Pause");
        pause.setOnAction(event -> controller.pause());


        // Stop all added tracks
        Button stop = new Button("Stop");
        stop.setOnAction(event -> controller.stop());


        // Currently non functional
        Button skipF = new Button(">>");
        Button skipB = new Button("<<");

        playerButtons.getChildren().addAll(skipB, play, pause, stop, skipF);


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
                                TrackLineGUI trackLine = new TrackLineGUI("New Track", getThis());
                                pane.getItems().add(trackLine.createTrack());
                                trackLine.addFile(file, 0);
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
            TrackLineGUI trackLine = new TrackLineGUI("New Track", this);
            pane.getItems().add(trackLine.createTrack());
            trackLine.addFile(file, 0);
        } catch(Exception e) {

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

    public SplitPane getSplitPane() {
        return splitPane;
    }
}
