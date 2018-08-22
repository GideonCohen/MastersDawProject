package GUI;

import Audio.DirectoryPlayer;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Directory view - displays a directory as a tree view on GUI. Wav files in the directory can be added to the editor.
 * Files may be dragged into the directory or deleted from it and the directory may be changed
 * Selected files may be previews using the directory player - (Alternative audio player, uses clips)
 */
public class DirectoryViewer{

    // Main GUI
    private FXGUIBuilder controller;
    // Base pane for the directory
    private StackPane directoryBase;
    // Directory view layout
    private BorderPane directory;
    // Path of root folder
    private File rootFolder;
    private Button maximise;
    // Currently selected file
    private File selected;


    /**
     * Constructor
     * @param controller
     */
    public DirectoryViewer(FXGUIBuilder controller) {
        this.controller = controller;
        // root folder
        rootFolder = new File("./");

    }

    /**
     * Make a return the directory view
     * @return - StackPane
     */
    public StackPane makeDirectory() {
        // Base of directory view
        directoryBase = new StackPane();
        directoryBase.getStyleClass().add("directory");
        // Actual tree view
        TreeView<File> treeView = new TreeView<>();
        treeView.getStyleClass().add("tree-view");

        directory = new BorderPane();

        // Create min/mac buttons
        Button minimise = new Button();
        Image minImage = new Image("Resources/min.png");
        minimise.setGraphic(new ImageView(minImage));

        maximise = new Button();
        Image maxImage = new Image("Resources/max.png");
        maximise.setGraphic(new ImageView(maxImage));

        maximise.setOnAction(event -> maximiseView());
        minimise.setOnAction(event -> minimiseView());

        // Create tree view
        DirectoryPlayer player = new DirectoryPlayer();
        treeView.setRoot(getNodesForDirectory(rootFolder));
        treeView.getRoot().setExpanded(true);
        // Allow drag/drop of wavs
        makeDraggable(treeView);
        HBox hBox = new HBox();

        // Create directory action bas
        Button play = new Button();
        Image playImage = new Image("Resources/playDir.png");
        play.setGraphic(new ImageView(playImage));

        Button pause = new Button();
        Image pauseImage = new Image("Resources/pauseDir.png");
        pause.setGraphic(new ImageView(pauseImage));

        Button stop = new Button();
        Image stopImage = new Image("Resources/stopDir.png");
        stop.setGraphic(new ImageView(stopImage));

        Button add = new Button();
        Image addImage = new Image("Resources/addDir.png");
        add.setGraphic(new ImageView(addImage));

        Button delete = new Button();
        Image deleteImage = new Image("Resources/delete.png");
        delete.setGraphic(new ImageView(deleteImage));


        // Change the root directory for the DAW
        Button changeRoot = new Button();
        Image changeImage = new Image("Resources/ChangeDir.png");
        changeRoot.setGraphic(new ImageView(changeImage));
        changeRoot.setOnAction(event -> selectRootFolder(treeView));


        // Set button actions
        play.setOnAction(e -> player.play());
        pause.setOnAction(e -> player.pause());
        stop.setOnAction(e -> player.stop());
        add.setOnAction(event -> {
            try {
                controller.addFile(selected);
            } catch (Exception e) {
                System.out.println("No file selected");
            }
        });
        delete.setOnAction(event -> deleteFile(selected, treeView));

        // Set tooltips
        play.setTooltip(new Tooltip("Play"));
        pause.setTooltip(new Tooltip("Pause"));
        stop.setTooltip(new Tooltip("Stop"));
        add.setTooltip(new Tooltip("Add to editor"));
        delete.setTooltip(new Tooltip("Delete File"));
        changeRoot.setTooltip(new Tooltip("Change root folder"));
        minimise.setTooltip(new Tooltip("Collapse"));
        maximise.setTooltip(new Tooltip("Expand"));

        hBox.getChildren().addAll(play, pause, stop, add, delete, changeRoot, minimise);
        directory.setBottom(hBox);
        directory.setCenter(treeView);


        // Only display the filename not the entire path.
        treeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
            public TreeCell<File> call(TreeView<File> t) {
                return new TreeCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((empty || item == null) ? "" : item.getName());
                    }
                };
            }
        });

        //add listeners to tree to play wav files.
        treeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, old_val, new_val) -> {
                    TreeItem<File> selectedItem = new_val;
                    try {
                        if (getFileExtension(selectedItem.getValue().getName()).equals("wav")) {
                            selected = new File(selectedItem.getValue().getAbsolutePath());
                            player.loadClip(selected);
                        }
                    } catch (NullPointerException e) {}
                        //player.play();
                });

        directoryBase.getChildren().add(directory);
        directoryBase.setMinWidth(200);
        return directoryBase;
    }

    /**
     * Creates a tree structure of a users directories from a given root.
     * It will filter out any file type that is not ".wav"
     * Adds event handlers to the wav files so that they can be previewed.
     *
     * @param directory
     * @return root
     */
    public TreeItem<File> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
        TreeItem<File> root = new TreeItem<>(directory);
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) { //Then we call the function recursively
                //add try catch statement to load complete directory.
                root.getChildren().add(getNodesForDirectory(f));
                //System.out.println("Loading " + f.getName());
            } else if (getFileExtension(f.getName()).equals("wav")) {
                TreeItem<File> wav = new TreeItem<>(f);
                root.getChildren().add(wav);
                //System.out.println("Loading " + f.getName());
            }
        }
        return root;
    }

    /**
     * Returns the file type. E.g. File "Fixuplooksharp.mp3" will return "mp3".
     * @param fullName - the files name.
     * @return - the files type
     */
    public static String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * Replace the directory view with the maximise button and shrink size
     */
    public void minimiseView() {
        directoryBase.getChildren().remove(directory);
        directoryBase.getChildren().add(maximise);
        directoryBase.setAlignment(Pos.BOTTOM_CENTER);
        directoryBase.setMinWidth(30);
    }

    /**
     * Show the main Directory view
     */
    public void maximiseView() {
        directoryBase.getChildren().remove(maximise);
        directoryBase.getChildren().add(directory);
        directoryBase.setMinWidth(200);
    }

    /**
     * Allow users to drag and drop wav files onto the treeview
     * @param tree - view to accept files
     */
    private void makeDraggable(TreeView tree){

        // Handler for drag over
        tree.setOnDragOver(new EventHandler<DragEvent>() {
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
        tree.setOnDragDropped(new EventHandler<DragEvent>() {
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
                                addFile(file, tree);
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
     * Add file to the path linked by the tree (currently hard coded)
     * @param file
     * @param tree
     * @throws IOException
     */
    public void addFile(File file, TreeView tree) throws IOException{
        File input = file;
        File outPut = rootFolder;
        // copy file
        FileUtils.copyFileToDirectory(input, outPut);
        tree.setRoot(getNodesForDirectory(rootFolder));
        tree.getRoot().setExpanded(true);

    }

    /**
     * Delete the currently selected file
     * @param file - selected file
     * @param tree - tree view
     */
    public void deleteFile(File file, TreeView tree) {
        if (selected != null) {
            boolean answer = ConfirmationBox.Display("Delete File", "Are you sure you want to delete this file?");
            if (answer) {
                //unselect current track
                selected = null;
                file.delete();
                // reset the root to update the contents
                tree.setRoot(getNodesForDirectory(rootFolder));
                tree.getRoot().setExpanded(true);
            }
        }
    }

    /**
     * Select a new root folder for the directory viewer
     * @param tree - treeview
     */
    public void selectRootFolder(TreeView tree){
        // alternative to file choose for directories
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select new root directory");
        chooser.setInitialDirectory(rootFolder);
        File newDirectory = chooser.showDialog(controller.getWindow());
        // set the new directory
        setRootFolder(newDirectory);
        // Set the new node
        tree.setRoot(getNodesForDirectory(rootFolder));
        tree.getRoot().setExpanded(true);

    }


    /**
     * Set the new root folder for the tree view
     * @param newFolder - the new root folder
     */
    public void setRootFolder(File newFolder) {
        rootFolder = newFolder;
    }
}
