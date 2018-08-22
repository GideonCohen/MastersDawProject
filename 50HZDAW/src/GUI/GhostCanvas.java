package GUI;

import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.io.File;

/**
 * Alternative Waveform canvas for creating ghosts when moving waveforms in the editor
 */
public class GhostCanvas {

    // Duration of track, used to calculate width
    private double durationInMilliSeconds;
    // Associated wav file
    private File file;
    // canvas length in pixels
    private int width;
    private Canvas canvas;
    // Starting time, used to calculate position
    private long start;
    // pixel to millisecond ration
    private double pixelRatio;

    private StackPane waveformStack;

    /**
     * Alternate waveform canvas for creating ghosts
     * @param duration
     * @param f
     * @param stack
     * @param start
     * @param pixRatio
     * @throws Exception
     */
    public GhostCanvas(double duration, File f, StackPane stack, long start, double pixRatio) throws Exception {

        // intialise global variables
        durationInMilliSeconds = duration;
        file = f;
        waveformStack = stack;
        this.start = start;
        pixelRatio = pixRatio;
    }


    /**
     * Create ghost canvas
     * @return - Ghost canvas
     */
    public Canvas createWaveform() {

        // +5 to help deal with padding issue in waveform generation
        width = (int) Math.round(durationInMilliSeconds * pixelRatio) + 5;
        // set width and create the canvas
        canvas = new Canvas(width, 150);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // get filepath and create waveform generator
        String filePath = file.getAbsolutePath();
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        //remove padding
        wf.setPaddingLeft(0);
        wf.setPaddingRight(0);
        wf.setWidth(width - 5);
        wf.setWaveAverageColor(Color.color(0.6	,0,0, 0.2));
        wf.setBackgroundColor(Color.color(1,1,1, 0.04));
        //System.out.println("padding is " + wf.getPaddingRight());
        wf.draw();

        //to set starting position for waveform
        canvas.setTranslateX(start * pixelRatio);
        //10 pixels = 1 second or 1000 ms
        //1 pixel = 0.1 second or 100ms
        //if setting position with ms, /100

        return canvas;
    }
}
