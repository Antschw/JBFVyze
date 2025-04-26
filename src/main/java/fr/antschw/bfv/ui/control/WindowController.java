package fr.antschw.bfv.ui.control;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WindowController {

    public void configureStage(Stage stage) {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
    }
}
