package GUI;

import Audio.DirectoryPlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;

public class DirectoryViewer{

    private JavaFXController controller;
    private StackPane directoryBase;
    private BorderPane directory;
    private StackPane minPane;

    public DirectoryViewer(JavaFXController controller) {
        this.controller = controller;
    }

    public StackPane makeDirectory() {
        directoryBase = new StackPane();
        TreeView<File> treeView = new TreeView<>();
        directory = new BorderPane();

        Button minimise = new Button("<<");
        Button maximise = new Button(">>");

        maximise.setOnAction(event -> maximiseView());
        minimise.setOnAction(event -> minimiseView());

        minPane = new StackPane();
        minPane.getChildren().add(maximise);

        DirectoryPlayer player = new DirectoryPlayer();
        treeView.setRoot(getNodesForDirectory(new File("50HZDAW/Samples")));
        treeView.getRoot().setExpanded(true);
        HBox hBox = new HBox();

        Button play = new Button("Play");
        Button pause = new Button("Pause");
        Button stop = new Button("Stop");

        play.setOnAction(e -> player.play());
        pause.setOnAction(e -> player.pause());
        stop.setOnAction(e -> player.stop());

        hBox.getChildren().addAll(play, pause, stop, minimise);
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
                    if (getFileExtension(selectedItem.getValue().getName()).equals("wav")) {
                        System.out.println("Playing: " + selectedItem.getValue().getName());
                        player.loadClip(new File(selectedItem.getValue().getAbsolutePath()));
                        //player.play();
                    }
                });

        directoryBase.getChildren().add(directory);
        directoryBase.setMinWidth(250);
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
        directoryBase.getChildren().add(minPane);
        directoryBase.setMinWidth(50);
    }

    public void maximiseView() {
        directoryBase.getChildren().remove(minPane);
        directoryBase.getChildren().add(directory);
        directoryBase.setMinWidth(200);
    }

}
