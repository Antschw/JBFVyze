package fr.antschw.bfv.adapters.input.window;

import fr.antschw.bfv.utils.I18nUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * Panel that displays the four status rows on the left
 * and, on the right, a titled, tight-spaced history list
 * with a distinct background.
 */
public class StatusPanel extends HBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();

    // --- Left status grid (unchanged logic) ---
    private final GridPane grid = new GridPane();
    private final ProgressIndicator ocrSpinner = new ProgressIndicator();
    private final Label ocrLabel = new Label();
    private final ProgressIndicator gtSpinner = new ProgressIndicator();
    private final Label gtLabel = new Label();
    private final ProgressIndicator hkSpinner = new ProgressIndicator();
    private final Label hkLabel = new Label();
    private final Label timeLabel = new Label();

    // --- Right history box ---
    private final ObservableList<String> historyData = FXCollections.observableArrayList();
    private final Label historyTitle = new Label(bundle.getString("server.history.title"));
    private final ListView<String> historyList = new ListView<>(historyData);

    public StatusPanel() {
        // HBox spacing between grid and history box
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(0, 0, 10, 0));

        // --- Configure status grid ---
        grid.setHgap(10);
        grid.setVgap(6);

        ocrSpinner.setPrefSize(16,16);
        ocrSpinner.setVisible(false);
        ocrLabel.getStyleClass().add("scan-label");

        gtSpinner.setPrefSize(16,16);
        gtSpinner.setVisible(false);
        gtLabel.getStyleClass().add("scan-label");

        hkSpinner.setPrefSize(16,16);
        hkSpinner.setVisible(false);
        hkLabel.getStyleClass().add("scan-label");

        timeLabel.getStyleClass().add("time-label");

        grid.addRow(0, ocrSpinner, ocrLabel);
        grid.addRow(1, gtSpinner, gtLabel);
        grid.addRow(2, hkSpinner, hkLabel);
        grid.addRow(3, new Label(), timeLabel);

        HBox.setHgrow(grid, Priority.ALWAYS);

        // --- Configure history box ---
        historyTitle.getStyleClass().add("header-label");

        // Fixer la taille de la liste d'historique
        historyList.setPrefWidth(120); // Largeur fixe
        historyList.setMaxWidth(120);  // Limite max
        historyList.setPrefHeight(120); // Hauteur fixe
        historyList.setMaxHeight(120);  // Limite max
        historyList.setFixedCellSize(22); // serrer les lignes
        historyList.getStyleClass().add("history-list"); // pour padding réduit

        VBox historyBox = new VBox(4, historyTitle, historyList);
        historyBox.getStyleClass().add("history-panel");
        historyBox.setPadding(new Insets(0, 4, 4, 4));
        historyBox.setPrefWidth(130); // Largeur fixe pour le VBox
        historyBox.setMaxWidth(130);  // Maximum width

        // Empêcher le redimensionnement
        HBox.setHgrow(historyBox, Priority.NEVER);

        // --- Assemble ---
        this.getChildren().addAll(grid, historyBox);
    }

    /** Add new server ID if not already in history. */
    public void addToHistory(String shortId) {
        String entry = "#" + shortId;
        if (!historyData.contains(entry)) {
            historyData.add(0, entry); // insérer en haut
        }
    }

    /** Reset only the status grid (not history). */
    public void reset() {
        ocrSpinner.setVisible(true);
        ocrLabel.setText(bundle.getString("server.ocr.running"));
        gtSpinner.setVisible(true);
        gtLabel.setText(bundle.getString("server.gametools.waiting"));
        hkSpinner.setVisible(true);
        hkLabel.setText(bundle.getString("server.hackers.waiting"));
        timeLabel.setText("");
    }

    public void setOcrStatus(String text, boolean running) {
        ocrSpinner.setVisible(running);
        ocrLabel.setText(text);
    }

    public void setGameToolsStatus(String text, boolean running) {
        gtSpinner.setVisible(running);
        gtLabel.setText(text);
    }

    public void setHackersStatus(String text, boolean running, boolean hasCheaters) {
        hkSpinner.setVisible(running);
        hkLabel.getStyleClass().removeAll("scan-success","scan-danger");
        hkLabel.getStyleClass().add(hasCheaters ? "scan-danger" : "scan-success");
        hkLabel.setText(text);
    }

    public void setTime(String text) {
        timeLabel.setText(text);
    }
}