package GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

public class PedalGUI {



    static ArrayList<Float> listOfValues;

    public static ArrayList<Float> Display() {

        listOfValues = new ArrayList<>();

        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(200);

        Label title = new Label("Please enter desired values: ");
        Label delayLabel = new Label("Delay");
        Label feedbackLabel = new Label("Feedback");
        Label fadeOutLabel = new Label("Fade out");


        TextField delayInput = new TextField("250");
        TextField feedbackInput = new TextField("5");
        TextField fadeOutInput = new TextField("0.8");
        Button confirm = new Button("Confirm");
        confirm.setOnAction(event -> {
            if ((verifyNum(delayInput, delayInput.getText()) && (verifyNum(feedbackInput, delayInput.getText()) &&
                    (verifyNum(fadeOutInput, delayInput.getText()))))) {
                listOfValues.add((float) Double.parseDouble(delayInput.getText()));
                listOfValues.add((float) Double.parseDouble(feedbackInput.getText()));
                listOfValues.add((float) Double.parseDouble(fadeOutInput.getText()));
                window.close();
            }
        });


        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, delayLabel, delayInput, feedbackLabel,
                feedbackInput, fadeOutLabel, fadeOutInput, confirm);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("GUI/Style.css");
        window.setScene(scene);
        //Show and wait - show and wait for it to be hidden or closed before it returns to main
        //Blocks user interaction until the alert box is closed
        window.showAndWait();


        return listOfValues;
    }

    /**
     * Checks if the input is a positive number or 0
     * @param input
     * @param message
     * @return
     */
    private static boolean verifyNum(TextField input, String message){
        boolean isNum;
        try{
            double delay = Double.parseDouble(input.getText());

            if (delay >= 0) {
                isNum = true;
                //System.out.println("The delay is " + delay);
            } else {
                isNum = false;
                //System.out.println("The delay cannot be negative");
                input.clear();
            }
        } catch(NumberFormatException e) {
            System.out.println(message + " is not a number");
            isNum = false;
            input.clear();
        }
        return isNum;
    }
}

