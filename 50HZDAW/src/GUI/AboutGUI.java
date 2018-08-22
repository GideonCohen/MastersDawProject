package GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutGUI {

    /**
     * Display the about message
     */
    public static void Display() {

        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("About this program");
        window.setMinWidth(350);
        window.setMinHeight(100);

        Label label1 = new Label("Created by Gideon Cohen, Thomas Martin, Patrick McKenna and Ivaan Nagen." +
                System.lineSeparator() + "Copyright 2018 University of Kent." +
                System.lineSeparator() + "Please find the user guide in the same directory as the JAR file for " +
                System.lineSeparator() + "information on how to use the program.");


        Button confirm = new Button("Accept");
        confirm.setOnAction(event -> {
            window.close();

        });


        VBox layout = new VBox(15);
        HBox layout2 = new HBox(15);

        layout2.getChildren().addAll(new Region(), label1, new Region());

        layout.getChildren().addAll(new Region(), layout2, confirm);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("GUI/Style.css");
        window.setScene(scene);
        //Show and wait - show and wait for it to be hidden or closed before it returns to main
        //Blocks user interaction until the alert box is closed
        window.showAndWait();

    }

    /**
     * Formats a string, namely the name of the file object, so that each line is
     * exactly 15 characters in length
     *
     * @return A string with formatted line length
     */
    public String formatName(String unformattedString) {

        int count = 0;
        String newStr = "";
        String[] str = unformattedString.split("");
        for (int i = 0; i < unformattedString.length(); i++) {
            if (count == 15) {
                newStr += System.lineSeparator();
                count = 0;
                newStr += str[i];
                count++;
            }
            else {
                newStr += str[i];
                count++;
            }
        }
        return newStr;
    }
}

