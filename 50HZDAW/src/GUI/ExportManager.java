package GUI;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ExportManager {

    /**
     * Export a .wav file from the current arrangment
     * @param window - the main window of the program
     * @return - File - the output .wav file
     */
    public File exportAsWAV(Stage window) {

        FileChooser FC = new FileChooser();

        // Create and add filter for only .wav files
        FileChooser.ExtensionFilter wavFilter =
                new FileChooser.ExtensionFilter("Wav Files (*.wav)", "*.wav");
        FC.getExtensionFilters().add(wavFilter);

        FC.setTitle("Select destination to save file");
        FC.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = FC.showSaveDialog(window);


        return file;
    }
}
