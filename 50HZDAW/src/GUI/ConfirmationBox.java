package GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationBox {

    // the result from the box
    static boolean answer;


    /**
     * @param title - title of the window
     * @param message - message to the user
     */
    public static boolean Display(String title, String message){
        Stage window = new Stage();

        // You must deal with this window before clicking on any other window in the application
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(200);

        Label label1 = new Label(message);

        //Answer buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            answer = true;
            window.close();
        });

        noButton.setOnAction(e -> {
            answer = false;
            window.close();
        });


        VBox layout = new VBox();
        layout.getChildren().addAll(label1, yesButton, noButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("GUI/Style.css");
        window.setScene(scene);
        //Show and wait - show and wait for it to be hidden or closed before it returns to main
        //Blocks user interaction until the alert box is closed
        window.showAndWait();

        return answer;
    }
}
