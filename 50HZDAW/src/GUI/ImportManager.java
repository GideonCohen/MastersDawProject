package GUI;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImportManager {

    public TrackLineGUI importFile(VBox channels, JavaFXController javaFXController, Stage window){
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
            TrackLineGUI trackLine = new TrackLineGUI(file.getName(), javaFXController);
            // Make a channel for the player
            channels.getChildren().add(trackLine.createTrack());
            trackLine.addFile(file);
            return trackLine;

        }
        catch(Exception e) {

        }

        return null;
    }
}
