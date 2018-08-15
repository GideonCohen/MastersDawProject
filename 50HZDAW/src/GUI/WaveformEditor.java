package GUI;

import Audio.Track;
import electronism.sample.gui.javafx.WaveformGenerator;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;


public class WaveformEditor {

    private double durationInMilliSeconds;
    private int index;
    private File file;
    private Track track;
    private StackPane waveformStack;
    private WaveformCanvas waveform;
    private double pixelRatio;

    public WaveformEditor(double duration, int index, File f, Track track, StackPane waveformStack, WaveformCanvas canvas, double pixelRatio){
        durationInMilliSeconds = duration;
        this.index = index;
        file = f;
        this.track = track;
        this.waveformStack = waveformStack;
        waveform = canvas;
        this.pixelRatio = pixelRatio;

        createEditor();
    }

    public void createEditor() {

        double width = durationInMilliSeconds*pixelRatio;
        Canvas canvas = new Canvas(width, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = file.getAbsolutePath();
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();

        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Waveform Editor");
        window.setMinWidth(200);
        window.setWidth(500);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_LEFT);
        ScrollPane sp = new ScrollPane();
        sp.setContent(canvas);

        Button duplicate = new Button("Duplicate sound");
        //duplicate.setOnAction(e -> duplicateSound(mixerSetUp, waveformSplit, index, durationInMilliSeconds, f));

        Button addPaddingButton = new Button("Set starting position");
        addPaddingButton.setOnAction(e -> {
            setDelay();
        });


        Button zoomButton = new Button("Zoom Out");
        zoomButton.setOnAction(e -> canvasZoom(canvas, filePath, true));

        Button zoomOutButton = new Button("Zoom");
        zoomOutButton.setOnAction(e -> canvasZoom(canvas, filePath, false));

        Label selectedTime = new Label("Selected time is: ");

        Label fileName = new Label("File Name: " + file.getName());
        Label fileLength = new Label("File Duration: " + durationInMilliSeconds/1000 + " seconds");

        layout.getChildren().addAll(sp, fileName, fileLength, duplicate, addPaddingButton, zoomOutButton, zoomButton, selectedTime);

        canvas.setOnMouseClicked(e -> {
            double x = e.getX() / canvas.getWidth();
            int y = (int) Math.round(x * durationInMilliSeconds);
            selectedTime.setText("Selected time is: " + y/1000 + " seconds");

        });

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.show();

    }

    public static void canvasZoom(Canvas canvas, String filePath, boolean increase) {

        try {
            canvas.setHeight(100);
            if (increase) {
                canvas.setWidth(canvas.getWidth() / 2);
            } else {
                canvas.setWidth(canvas.getWidth() * 2);
            }
            GraphicsContext g = canvas.getGraphicsContext2D();
            WaveformGenerator w = new WaveformGenerator(new File(filePath), g);
            w.draw();
        } catch (RuntimeException e) {
            System.out.println("Reached zoom maximum");
        }


    }

    /*
    public void duplicateSound(MixerSetUp mixerSetUp, HBox waveformSplit, int index, int durationInMilliSeconds, File f) {

        HashMap<Integer, Track> map = mixerSetUp.getFileToTrackMap();
        Track t = map.get(index);

        ArrayList<AudioTrackData> data = t.getAudioTrackData();
        AudioTrackData a = data.get(0);
        ArrangementHelper AH = new ArrangementHelper();
        byte[] newArray = AH.addTwoArrays(a.getStereoByteArray(), a.getOriginalArray());
        a.setStereoByteArray(newArray);
        data.add(0, a);
        t.addDataToTrack();


        double newWidth = newArray.length * 0.0000056689;
        newWidth = newWidth * 10;

        waveformSplit.getChildren().clear();

        Canvas canvas = new Canvas(newWidth, 100);

        addMouseListeners(canvas, durationInMilliSeconds, index, f, mixerSetUp, waveformSplit);

        GraphicsContext context = canvas.getGraphicsContext2D();
        WaveformGenerator WG = new WaveformGenerator(newArray, context);
        WG.draw();

        waveformSplit.getChildren().add(canvas);

    }
*/


    public void setDelay() {


       long delay = (long) (DelayBox.Display(waveform.getCanvas().getTranslateX(), pixelRatio) * 1000);
        System.out.println("Delay = " + (delay) + "ms");
        System.out.println(delay*pixelRatio + " is position");
        waveform.setPosition(delay);
        System.out.println(delay);
        track.moveAudioFile(index, delay);

    }

/*
    public void getPaddingAmount() {

        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(200);

        Label label = new Label("Please selected length of padding (in ms)");
        TextField textField = new TextField();

        Button button = new Button("Accept");
        button.setOnAction(e ->
        {
            setPadding(Integer.parseInt(textField.getCharacters().toString()));
            window.close();
        });

        VBox layout = new VBox();
        layout.getChildren().addAll(label, textField, button);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.showAndWait();
    }

    public void setPadding(int p) {
        padding = p;
    }
    */


    public void addMouseListeners(Canvas canvas, int width) {

        canvas.setCursor(Cursor.HAND);
        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if (button == MouseButton.SECONDARY) {
                    WaveformEditor w = new WaveformEditor(durationInMilliSeconds, index, file, track, waveformStack, waveform, pixelRatio);
                }
            }
        });


    }

}

