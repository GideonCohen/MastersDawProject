package GUI;

import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImportManager {

    public TrackLineGUI importFile(VBox channels, FXGUIBuilder FXGUIBuilder, Stage window){
        // JavaFX prebuilt file chooser
        FileChooser fileChooser = new FileChooser();

        // Create and add filter for only .wav files
        FileChooser.ExtensionFilter wavFilter =
                new FileChooser.ExtensionFilter("Wav Files (*.wav)", "*.wav");
        fileChooser.getExtensionFilters().add(wavFilter);

        fileChooser.setTitle("Choose a Track");
        File file = fileChooser.showOpenDialog(window);

        // If appropriate file type is chosen
        TrackLineGUI trackLine = new TrackLineGUI(file.getName(), FXGUIBuilder);
        try {
            // Make a channel for the player
            channels.getChildren().add(trackLine.createTrack());
            trackLine.addFile(file);

        }
        catch(Exception e) {

        }

        return trackLine;
    }
}
