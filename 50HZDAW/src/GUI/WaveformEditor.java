package GUI;

import Audio.ArrangementHelper;
import Audio.AudioTrackData;
import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class WaveformEditor {

    public WaveformEditor(int durationInSeconds, File f, double d) {

        //try to create waveform for file, for now .wav files need to be in directory specified below
        int width = Math.round(durationInSeconds);
        Canvas canvas = new Canvas(width * 10, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = f.getAbsolutePath();
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();


        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Waveform Editor");
        window.setMinWidth(200);
        window.setWidth(500);

        VBox layout = new VBox();
        ScrollPane sp = new ScrollPane();
        sp.setContent(canvas);

        Button zoomButton = new Button("Zoom");
        zoomButton.setOnAction(e -> canvasZoom(canvas, filePath, true));

        Button zoomOutButton = new Button("Zoom out");
        zoomOutButton.setOnAction(e -> canvasZoom(canvas, filePath, false));

        Label selectedTime = new Label("Selected time is: ");

        layout.getChildren().addAll(sp, zoomButton, zoomOutButton, selectedTime);

        canvas.setOnMouseClicked(e -> {
            double x = e.getX() / canvas.getWidth();
            int y = (int) Math.round(x * durationInSeconds);
            selectedTime.setText("Selected time is: " + y + " seconds");

        });

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.show();

    }

    public static void canvasZoom(Canvas canvas, String filePath, boolean increase) {

        canvas.setHeight(100);
        if (increase) {
            canvas.setWidth(canvas.getWidth() / 2);
        } else {
            canvas.setWidth(canvas.getWidth() * 2);
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        WaveformGenerator w = new WaveformGenerator(new File(filePath), g);
        w.draw();


    }
}

