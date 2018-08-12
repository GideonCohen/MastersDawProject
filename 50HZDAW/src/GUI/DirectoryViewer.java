package GUI;

import Audio.DirectoryPlayer;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DirectoryViewer{

    private JavaFXController controller;
    private StackPane directoryBase;
    private BorderPane directory;

    private Button maximise;

    private File selected;


    public DirectoryViewer(JavaFXController controller) {
        this.controller = controller;
    }

    public StackPane makeDirectory() {
        directoryBase = new StackPane();
        directoryBase.getStyleClass().add("directory");
        TreeView<File> treeView = new TreeView<>();
        directory = new BorderPane();
        directory.getStyleClass().add("directory");

        Button minimise = new Button();
        Image minImage = new Image("Resources/min.png");
        minimise.setGraphic(new ImageView(minImage));

        maximise = new Button();
        Image maxImage = new Image("Resources/max.png");
        maximise.setGraphic(new ImageView(maxImage));

        maximise.setOnAction(event -> maximiseView());
        minimise.setOnAction(event -> minimiseView());


        DirectoryPlayer player = new DirectoryPlayer();
        treeView.setRoot(getNodesForDirectory(new File("50HZDAW/Samples")));
        treeView.getRoot().setExpanded(true);
        makeDraggable(treeView);
        HBox hBox = new HBox();

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

        play.setTooltip(new Tooltip("Play"));
        pause.setTooltip(new Tooltip("Pause"));
        stop.setTooltip(new Tooltip("Stop"));
        add.setTooltip(new Tooltip("Add to editor"));
        delete.setTooltip(new Tooltip("Delete File"));
        minimise.setTooltip(new Tooltip("Collapse"));
        maximise.setTooltip(new Tooltip("Expand"));

        hBox.getChildren().addAll(play, pause, stop, add, delete, minimise);
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
                System.out.println("Loading " + f.getName());
            } else if (getFileExtension(f.getName()).equals("wav")) {
                TreeItem<File> wav = new TreeItem<>(f);
                root.getChildren().add(wav);
                System.out.println("Loading " + f.getName());
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

    public void minimiseView() {
        directoryBase.getChildren().remove(directory);
        directoryBase.getChildren().add(maximise);
        directoryBase.setAlignment(Pos.BOTTOM_CENTER);
        directoryBase.setMinWidth(30);
    }

    public void maximiseView() {
        directoryBase.getChildren().remove(maximise);
        directoryBase.getChildren().add(directory);
        directoryBase.setMinWidth(200);
    }

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

    public void addFile(File file, TreeView tree) throws IOException{
        File input = file;
        File outPut = new File("50HZDAW/Samples");
        FileUtils.copyFileToDirectory(input, outPut);
        tree.setRoot(getNodesForDirectory(new File("50HZDAW/Samples")));
        tree.getRoot().setExpanded(true);

    }

    public void deleteFile(File file, TreeView tree) {
        if (selected != null) {
            boolean answer = ConfirmationBox.Display("Delete File", "Are you sure you want to delete this file?");
            if (answer) {
                selected = null;
                file.delete();
                tree.setRoot(getNodesForDirectory(new File("50HZDAW/Samples")));
                tree.getRoot().setExpanded(true);
            }
        }
    }
}
