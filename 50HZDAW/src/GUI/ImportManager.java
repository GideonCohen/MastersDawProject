package GUI;

import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImportManager {

    public void importFile(SplitPane pane, JavaFXController javaFXController, Stage window){
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
            TrackLineGUI trackLine = new TrackLineGUI("New Track", javaFXController);
            pane.getItems().add(trackLine.createTrack());
            trackLine.addFile(file, 0);

        }
        catch(Exception e) {

        }
    }
}
