package fr.antschw.bfv.ui.control;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class ResizeController {

    private static final int RESIZE_MARGIN = 10;

    public void makeResizable(Stage stage, Region root) {
        root.setOnMouseMoved(event -> {
            if (isInResizeZone(event, root)) {
                root.setCursor(Cursor.SE_RESIZE);
            } else {
                root.setCursor(Cursor.DEFAULT);
            }
        });

        root.setOnMouseDragged(event -> {
            if (root.getCursor() == Cursor.SE_RESIZE) {
                double newWidth = event.getSceneX();
                double newHeight = event.getSceneY();
                if (newWidth > 300) stage.setWidth(newWidth);
                if (newHeight > 300) stage.setHeight(newHeight);
            }
        });
    }

    private boolean isInResizeZone(MouseEvent event, Region root) {
        return event.getX() >= (root.getWidth() - RESIZE_MARGIN) &&
               event.getY() >= (root.getHeight() - RESIZE_MARGIN);
    }
}
