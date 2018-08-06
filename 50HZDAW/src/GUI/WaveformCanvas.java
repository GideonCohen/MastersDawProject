package GUI;

import Audio.MixerSetUp;
import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.File;

public class WaveformCanvas {

    private double durationInMilliSeconds;
    private File file;
    private int width;
    private Canvas canvas;

    private int index;
    private long position;

    private Track track;
    private MixerSetUp mixer;

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    private StackPane waveformStack;


    public WaveformCanvas(double duration, File f, int i, MixerSetUp mixerSetUp, StackPane stack, long delay, Track track) throws Exception {

        index = i;
        durationInMilliSeconds = duration;
        file = f;
        mixer = mixerSetUp;
        waveformStack = stack;
        position = delay;
        this.track = track;

    }



    public Canvas createWaveform() {

        width = (int) Math.round(durationInMilliSeconds/10);
        canvas = new Canvas(width, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = file.getAbsolutePath();
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.setPaddingLeft(0);
        wf.setPaddingRight(0);
        wf.setWidth(width);
        wf.setShowCenterLine(true);
        System.out.println("padding is " + wf.getPaddingRight());
        wf.draw();

        //to set starting position for waveform
        canvas.setTranslateX(position/10);
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
                    WaveformEditor w = new WaveformEditor(width, index, file, track, waveformStack, canvas);
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

                    if (newTranslateX < 0) {
                        newTranslateX = 0;
                    }
                    ((Canvas) (event.getSource())).setTranslateX(newTranslateX);

                    long delay = (long) newTranslateX * 10;
                    track.moveAudioFile(index, delay);
                    System.out.println("Start at " + delay + "ms");
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
