package GUI;

import Audio.MixerSetUp;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;

public class WaveformCanvas {

    private double durationInSeconds;
    private File file;
    private int width;
    private Canvas canvas;

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    public WaveformCanvas(double d, File f) {
        durationInSeconds = d;
        file = f;
    }

    public Canvas createWaveform() {
        width = (int) Math.round(durationInSeconds);
        canvas = new Canvas(width * 10, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = file.getAbsolutePath();

        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();

        //to set starting position for waveform
        canvas.setTranslateX(0);
        //10 pixels = 1 second or 1000 ms
        //1 pixel = 0.1 second or 100ms
        //if setting position with ms, /100

        addMouseListeners();
        return canvas;
    }

    public void addMouseListeners() {

        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.SECONDARY) {
                    WaveformEditor w = new WaveformEditor(width, file, durationInSeconds);
                }
            }
        });

        canvas.setCursor(Cursor.HAND);
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    double offsetX = event.getSceneX() - orgSceneX;
                    double offsetY = event.getSceneY() - orgSceneY;
                    double newTranslateX = orgTranslateX + offsetX;
                    double newTranslateY = orgTranslateY + offsetY;

                    if (newTranslateX < 0) {
                        newTranslateX = 0;
                    }
                    ((Canvas) (event.getSource())).setTranslateX(newTranslateX);
                    //((Canvas)(t.getSource())).setTranslateY(newTranslateY);

                }
            }
        });

        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    orgSceneX = event.getSceneX();
                    //orgSceneY = t.getSceneY();
                    orgTranslateX = ((Canvas) (event.getSource())).getTranslateX();
                    //orgTranslateY = ((Canvas)(t.getSource())).getTranslateY();

                }
            }

        });
    }
}
