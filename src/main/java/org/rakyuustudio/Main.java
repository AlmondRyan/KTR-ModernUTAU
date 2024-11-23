package org.rakyuustudio;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.rakyuustudio.pianoroll.PianoRollFX;
import org.rakyuustudio.pianoroll.PianoRollInterface;
import org.rakyuustudio.pianoroll.PianoRollFactory;

public class Main extends Application {
    private PianoRollInterface pianoRoll;

    @Override
    public void start(Stage primaryStage) {
        try {
            pianoRoll = PianoRollFactory.createPianoRoll();
            Scene scene = new Scene((PianoRollFX)pianoRoll, 1000, 600);
            primaryStage.setTitle("Piano Roll - JavaFX");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}