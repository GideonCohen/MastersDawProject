package GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static java.lang.Math.abs;

public class DelayBox {

    // the result from the box
    static double delay;

    public static double Display(double currentPos){
        delay = currentPos;

        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Delay Box");
        window.setMinWidth(200);

        Label label1 = new Label("Please enter a delay in seconds");


        TextField delayInput = new TextField();
        Button confirm = new Button("Confirm");
        confirm.setOnAction(event -> {
            if (verifyNum(delayInput, delayInput.getText())) {
                delay = Double.parseDouble(delayInput.getText());
                window.close();
            }
        });


        VBox layout = new VBox();
        layout.getChildren().addAll(label1, delayInput, confirm);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("GUI/Style.css");
        window.setScene(scene);
        //Show and wait - show and wait for it to be hidden or closed before it returns to main
        //Blocks user interaction until the alert box is closed
        window.showAndWait();


        return delay;
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
