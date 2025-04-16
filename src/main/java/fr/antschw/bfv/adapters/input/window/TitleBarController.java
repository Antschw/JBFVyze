package fr.antschw.bfv.adapters.input.window;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller responsible for creating the custom title bar with drag, minimize, and close functionality.
 */
public class TitleBarController {

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Creates the title bar HBox with title label and control buttons.
     *
     * @param stage the Stage to control
     * @return configured HBox title bar
     */
    public HBox createTitleBar(Stage stage) {
        Label title = new Label("BFVyze");
        title.getStyleClass().add("title-label");

        Button minimizeButton = createControlButton(stage, "mdi2m-minus", false);
        Button closeButton = createControlButton(stage, "mdi2c-close", true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightBox = new HBox(10, minimizeButton, closeButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox titleBar = new HBox(10);
        titleBar.getChildren().addAll(title, spacer, rightBox);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPrefHeight(30);
        titleBar.getStyleClass().add("title-bar");

        addDragEvents(stage, titleBar);

        return titleBar;
    }

    /**
     * Creates a control button with Ikonli icon.
     *
     * @param stage        the Stage to control
     * @param iconLiteral  Ikonli icon literal
     * @param isClose      if true, sets action to close
     * @return configured Button
     */
    private Button createControlButton(Stage stage, String iconLiteral, boolean isClose) {
        Button button = new Button();
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        button.setGraphic(icon);
        button.getStyleClass().add("control-button");

        if (isClose) {
            button.setOnAction(e -> stage.close());
        } else {
            button.setOnAction(e -> stage.setIconified(true));
        }

        return button;
    }

    /**
     * Adds mouse events to allow dragging the window.
     *
     * @param stage the Stage to move
     * @param node  the node to attach events
     */
    private void addDragEvents(Stage stage, HBox node) {
        node.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            node.setCursor(Cursor.MOVE);
        });

        node.setOnMouseReleased(event -> node.setCursor(Cursor.DEFAULT));

        node.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
