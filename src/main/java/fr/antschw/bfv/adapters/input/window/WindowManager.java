package fr.antschw.bfv.adapters.input.window;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WindowManager {

    public void configureStage(Stage stage) {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
    }
}
