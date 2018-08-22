package GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorMessageGUI {

    public static void Display() {

        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Error");
        window.setMinWidth(200);
        window.setMinHeight(100);

        Label label1 = new Label("Please enter a valid value");
        Label label2 = new Label("Effect not applied");


        Button confirm = new Button("Accept");
        confirm.setOnAction(event -> {
            window.close();

        });


        VBox layout = new VBox(15);
        VBox layout2 = new VBox(5);

        layout2.getChildren().addAll(label1, label2);
        layout2.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(layout2, confirm);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("GUI/Style.css");
        window.setScene(scene);
        //Show and wait - show and wait for it to be hidden or closed before it returns to main
        //Blocks user interaction until the alert box is closed
        window.showAndWait();

    }
}
