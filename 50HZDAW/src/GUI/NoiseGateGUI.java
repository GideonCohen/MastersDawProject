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

public class NoiseGateGUI {



    static ArrayList<Float> listOfValues;

    /**
     * Popout window for the noise gate, Takes values from the user and applies the effect
     * @return Float array with effect added
     */
    public static ArrayList<Float> Display() {

        listOfValues = new ArrayList<>();

        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(200);

        Label title = new Label("Please enter desired values: ");
        Label attack = new Label("Attack");
        Label release = new Label("Release");
        Label threshold = new Label("Threshold");


        TextField attackInput = new TextField("250");
        TextField releaseInput = new TextField("500");
        TextField thresholdInput = new TextField("0.05");
        Button confirm = new Button("Confirm");
        confirm.setOnAction(event -> {
            if ((verifyNum(attackInput, attackInput.getText()) && (verifyNum(releaseInput, attackInput.getText()) &&
                    (verifyNum(thresholdInput, attackInput.getText()))))) {
                listOfValues.add((float) Double.parseDouble(attackInput.getText()));
                listOfValues.add((float) Double.parseDouble(releaseInput.getText()));
                listOfValues.add((float) Double.parseDouble(thresholdInput.getText()));
                window.close();
            }
        });


        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, attack, attackInput, release,
             releaseInput, threshold, thresholdInput, confirm);
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

