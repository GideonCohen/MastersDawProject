package GUI;

import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;

public class WaveformCanvas {

    // Duration of track, used to calculate width
    private double durationInMilliSeconds;
    // Associated wav file
    private File file;
    // canvas length in pixels
    private int width;
    private Canvas canvas;
    // index of associated file
    private int index;
    // Starting time, used to calculate position
    private long start;
    // associated track object
    private Track track;
    // pixel to millisecond ration
    private double pixelRatio;
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    private GhostCanvas ghostCanvas;
    private Canvas ghost;

    private StackPane waveformStack;

    private TrackLineGUI trackLineGUI;

    private WaveformGenerator wf;


    public WaveformCanvas(double duration, File f, int i, StackPane stack, long start, Track track, double pixRatio, TrackLineGUI trackLine){

        // intialise global variables
        index = i;
        durationInMilliSeconds = duration;
        file = f;
        waveformStack = stack;
        this.start = start;
        this.track = track;
        pixelRatio = pixRatio;
        trackLineGUI = trackLine;
    }


    public Canvas createWaveform() {

        // +5 to help deal with padding issue in waveform generation
        width = (int) Math.round(durationInMilliSeconds * pixelRatio) + 5;
        // set width and create the canvas
        canvas = new Canvas(width, 150);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // get filepath and create waveform generator
        String filePath = file.getAbsolutePath();
        wf = new WaveformGenerator(new File(filePath), gc);
        //remove padding
        wf.setPaddingLeft(0);
        wf.setPaddingRight(0);
        wf.setWidth(width - 5);
        wf.setWaveAverageColor(Color.MIDNIGHTBLUE);
        wf.setBackgroundColor(Color.color(1,1,1, 0.2));
        //System.out.println("padding is " + wf.getPaddingRight());
        wf.draw();

        //to set starting position for waveform
        canvas.setTranslateX(start * pixelRatio);
        //10 pixels = 1 second or 1000 ms
        //1 pixel = 0.1 second or 100ms
        //if setting position with ms, /100

        addMouseListeners(canvas);
        return canvas;
    }


    /**
     * Adds interactivity to the canvas
     */
    public void addMouseListeners(Canvas canvas) {

        WaveformCanvas waveform = this;

        // Open the waveform editor when the canvas is right clicked
        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.SECONDARY) {
                    WaveformEditor w = new WaveformEditor(durationInMilliSeconds, index, file, track, waveformStack, waveform, trackLineGUI);
                }
            }
        });

        // Set cursor to hand when on the canvas
        canvas.setCursor(Cursor.HAND);
        // Move the canvas and update the delay when dragged
        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    double offsetX = event.getSceneX() - orgSceneX;
                    //double offsetY = event.getSceneY() - orgSceneY;
                    double newTranslateX = orgTranslateX + offsetX;

                    if (newTranslateX < 0) {
                        newTranslateX = 0;
                    }
                    ((Canvas) (event.getSource())).setTranslateX(newTranslateX);
                    orgTranslateX = newTranslateX;

                    long delay = (long) (newTranslateX/pixelRatio);
                    start = delay;
                    track.moveAudioFile(index, delay);
                    System.out.println("Start at " + delay + "ms");
                    updateProcessing(); // update processing after audio file is moved, otherwise original signal is played back.
                    waveformStack.getChildren().remove(ghost);

                }
            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    double offsetX = event.getSceneX() - orgSceneX;
                    //double offsetY = event.getSceneY() - orgSceneY;
                    double newTranslateX = orgTranslateX + offsetX;

                    if (newTranslateX < 0) {
                        newTranslateX = 0;
                    }
                    ghost.setTranslateX(newTranslateX);

                }
            }
        });


        // get the position of the canvas on click
        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    orgSceneX = event.getSceneX();
                    //orgSceneY = t.getSceneY();
                    orgTranslateX = ((Canvas) (event.getSource())).getTranslateX();
                    //orgTranslateY = ((Canvas)(t.getSource())).getTranslateY();
                    try {
                        ghostCanvas = new GhostCanvas(durationInMilliSeconds, file, waveformStack, start, pixelRatio);
                    } catch (Exception e) {}
                    ghost = ghostCanvas.createWaveform();
                    waveformStack.getChildren().add(ghost);
                }
            }
        });
    }

    /**
     *  Update processing every time audio file is moved. Volume, pan, delay, distortion.
     */

    public void updateProcessing () {
        float newVol = track.getVolume();
        float leftPan = track.getLeftPan();
        float rightPan = track.getRightPan();
        if(track.getReverse()) {
            track.setReverse();
        }
        track.addVolume(newVol);
        track.setPan(leftPan, rightPan);
    }


    public Canvas getCanvas() {
        return canvas;
    }

    public void setPixelRatio(double pixelRatio) {
        this.pixelRatio = pixelRatio;
    }

    public long getPosition() {
        return start;
    }

    public void setPosition(long start) {
        canvas.setTranslateX(start*pixelRatio);
    }

    public double getPixelRatio() {
        return pixelRatio;
    }

    public void setCanvas(Canvas canvas) {
        waveformStack.getChildren().remove(this.canvas);
        this.canvas = canvas;
        waveformStack.getChildren().add(this.canvas);
        addMouseListeners(this.canvas);
        start = (long) (canvas.getTranslateX()/pixelRatio);
        track.moveAudioFile(index, start);

    }
}
