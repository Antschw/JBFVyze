package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.domain.model.HackersSummary;
import fr.antschw.bfv.ui.component.TimerComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * Panel that displays the server information and player statistics in a responsive layout,
 * alongside a history section showing previously scanned server IDs.
 * Maintenant avec support pour TimerComponent.
 */
public class StatusPanel extends HBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();

    // Spinners for server info
    private final ProgressIndicator ocrSpinner = new ProgressIndicator();
    private final ProgressIndicator gtSpinner = new ProgressIndicator();

    // Spinners for player categories
    private final ProgressIndicator hackersSpinner = new ProgressIndicator();
    private final ProgressIndicator verySusSpinner = new ProgressIndicator();
    private final ProgressIndicator susSpinner = new ProgressIndicator();
    private final ProgressIndicator legitSpinner = new ProgressIndicator();

    // Labels for server information
    private final Label ocrLabelText = new Label(bundle.getString("server.server_number"));
    private final Label gtLabelText = new Label(bundle.getString("server.long_server_number"));
    private final Label ocrValueLabel = new Label("—");
    private final Label gtValueLabel = new Label("—");

    // Labels for player categories
    private final Label hackersLabelText = new Label(bundle.getString("server.hackers.label"));
    private final Label verySusLabelText = new Label(bundle.getString("server.very_sus.label"));
    private final Label susLabelText = new Label(bundle.getString("server.sus.label"));
    private final Label legitLabelText = new Label(bundle.getString("server.legit.label"));

    private final Label hackersValueLabel = new Label("0");
    private final Label verySusValueLabel = new Label("0");
    private final Label susValueLabel = new Label("0");
    private final Label legitValueLabel = new Label("0");

    // Timer component (sera injecté par ServerView)
    private TimerComponent timeComponent;

    // Container for player categories that adapts to window width
    private final GridPane categoryGrid = new GridPane();

    // Container for server info
    private final GridPane serverInfoGrid = new GridPane();

    // Main container that holds server info and categories
    private final VBox mainContainer = new VBox(10);

    // History data
    private final ObservableList<String> historyData = FXCollections.observableArrayList();
    private final VBox historyBox = new VBox();

    // Minimum width for horizontal layout
    private static final double MIN_WIDTH_FOR_HORIZONTAL = 500;

    public StatusPanel() {
        // HBox spacing between grid and history box
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(0, 0, 10, 0));

        // Setup server info grid
        configureServerInfoGrid();

        // Setup player categories grid with responsive layout
        configureCategoryGrid();

        // Setup history box
        configureHistoryBox();

        // Add components to main container
        mainContainer.getChildren().addAll(serverInfoGrid, categoryGrid);

        // Add main container and history box to HBox
        HBox.setHgrow(mainContainer, Priority.ALWAYS);
        this.getChildren().addAll(mainContainer, historyBox);

        // Add listener for width changes to adjust layout
        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout(newVal.doubleValue()));
    }

    /**
     * Configure the server info grid
     */
    private void configureServerInfoGrid() {
        serverInfoGrid.setHgap(15);
        serverInfoGrid.setVgap(10);

        // Set column constraints for alignment
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(120);

        ColumnConstraints spinnerColumn = new ColumnConstraints();
        spinnerColumn.setMaxWidth(25);
        spinnerColumn.setPrefWidth(25);

        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);

        serverInfoGrid.getColumnConstraints().addAll(labelColumn, spinnerColumn, valueColumn);

        // Configure spinners
        ocrSpinner.setPrefSize(16, 16);
        gtSpinner.setPrefSize(16, 16);

        // Hide spinners by default
        ocrSpinner.setVisible(false);
        gtSpinner.setVisible(false);

        // Style server info labels
        ocrLabelText.getStyleClass().add("status-label");
        gtLabelText.getStyleClass().add("status-label");
        ocrValueLabel.getStyleClass().add("status-value");
        gtValueLabel.getStyleClass().add("status-value");

        // Add to grid
        serverInfoGrid.addRow(0, ocrLabelText, ocrSpinner, ocrValueLabel);
        serverInfoGrid.addRow(1, gtLabelText, gtSpinner, gtValueLabel);
    }

    /**
     * Configure the player categories grid
     */
    private void configureCategoryGrid() {
        categoryGrid.setHgap(15);
        categoryGrid.setVgap(10);
        categoryGrid.getStyleClass().add("category-grid");

        // Configure spinners
        hackersSpinner.setPrefSize(16, 16);
        verySusSpinner.setPrefSize(16, 16);
        susSpinner.setPrefSize(16, 16);
        legitSpinner.setPrefSize(16, 16);

        // Hide spinners by default
        hackersSpinner.setVisible(false);
        verySusSpinner.setVisible(false);
        susSpinner.setVisible(false);
        legitSpinner.setVisible(false);

        // Style player category labels
        hackersLabelText.getStyleClass().addAll("status-label", "status-hackers");
        verySusLabelText.getStyleClass().addAll("status-label", "status-verysus");
        susLabelText.getStyleClass().addAll("status-label", "status-sus");
        legitLabelText.getStyleClass().addAll("status-label", "status-legit");

        hackersValueLabel.getStyleClass().addAll("status-value", "status-hackers");
        verySusValueLabel.getStyleClass().addAll("status-value", "status-verysus");
        susValueLabel.getStyleClass().addAll("status-value", "status-sus");
        legitValueLabel.getStyleClass().addAll("status-value", "status-legit");

        // Initially set up in vertical layout
        setVerticalCategoryLayout();
    }

    /**
     * Configure the history box
     */
    private void configureHistoryBox() {
        Label historyTitle = new Label(bundle.getString("server.history.title"));
        historyTitle.getStyleClass().add("header-label");

        ListView<String> historyList = new ListView<>(historyData);
        historyList.setPrefWidth(120);
        historyList.setMaxWidth(120);
        historyList.setPrefHeight(120);
        historyList.setFixedCellSize(22);
        historyList.getStyleClass().add("history-list");

        historyBox.getChildren().addAll(historyTitle, historyList);
        historyBox.setSpacing(4);
        historyBox.getStyleClass().add("history-panel");
        historyBox.setPadding(new Insets(0, 4, 4, 4));
        historyBox.setPrefWidth(130);
        historyBox.setMaxWidth(130);

        VBox.setVgrow(historyList, Priority.ALWAYS);
    }

    /**
     * Set up vertical layout for player categories (for narrow windows)
     */
    private void setVerticalCategoryLayout() {
        categoryGrid.getChildren().clear();
        categoryGrid.getColumnConstraints().clear();

        // Define column constraints
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(120);

        ColumnConstraints spinnerColumn = new ColumnConstraints();
        spinnerColumn.setMaxWidth(25);
        spinnerColumn.setPrefWidth(25);

        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);

        categoryGrid.getColumnConstraints().addAll(labelColumn, spinnerColumn, valueColumn);

        // Add rows in vertical layout
        categoryGrid.addRow(0, hackersLabelText, hackersSpinner, hackersValueLabel);
        categoryGrid.addRow(1, verySusLabelText, verySusSpinner, verySusValueLabel);
        categoryGrid.addRow(2, susLabelText, susSpinner, susValueLabel);
        categoryGrid.addRow(3, legitLabelText, legitSpinner, legitValueLabel);
    }

    /**
     * Set up horizontal layout for player categories (for wider windows)
     */
    private void setHorizontalCategoryLayout() {
        categoryGrid.getChildren().clear();
        categoryGrid.getColumnConstraints().clear();

        // Define column constraints for first row
        ColumnConstraints labelCol1 = new ColumnConstraints();
        labelCol1.setMinWidth(120);

        ColumnConstraints spinnerCol1 = new ColumnConstraints();
        spinnerCol1.setPrefWidth(25);

        ColumnConstraints valueCol1 = new ColumnConstraints();
        valueCol1.setPrefWidth(60);

        // Spacer column
        ColumnConstraints spacerCol = new ColumnConstraints();
        spacerCol.setPrefWidth(30);

        // Define column constraints for second row
        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(120);

        ColumnConstraints spinnerCol2 = new ColumnConstraints();
        spinnerCol2.setPrefWidth(25);

        ColumnConstraints valueCol2 = new ColumnConstraints();
        valueCol2.setHgrow(Priority.ALWAYS);

        categoryGrid.getColumnConstraints().addAll(
            labelCol1, spinnerCol1, valueCol1,
            spacerCol,
            labelCol2, spinnerCol2, valueCol2
        );

        // Add in horizontal layout (2 rows of 2 categories each)
        categoryGrid.add(hackersLabelText, 0, 0);
        categoryGrid.add(hackersSpinner, 1, 0);
        categoryGrid.add(hackersValueLabel, 2, 0);

        categoryGrid.add(susLabelText, 4, 0);
        categoryGrid.add(susSpinner, 5, 0);
        categoryGrid.add(susValueLabel, 6, 0);

        categoryGrid.add(verySusLabelText, 0, 1);
        categoryGrid.add(verySusSpinner, 1, 1);
        categoryGrid.add(verySusValueLabel, 2, 1);

        categoryGrid.add(legitLabelText, 4, 1);
        categoryGrid.add(legitSpinner, 5, 1);
        categoryGrid.add(legitValueLabel, 6, 1);
    }

    /**
     * Update layout based on available width
     */
    private void updateLayout(double width) {
        // Calculate space available for main container
        double mainContainerWidth = width - historyBox.getWidth() - getSpacing();

        // Adjust history box height based on layout
        if (mainContainerWidth >= MIN_WIDTH_FOR_HORIZONTAL) {
            setHorizontalCategoryLayout();
            historyBox.setPrefHeight(175); // Shorter height for horizontal layout
        } else {
            setVerticalCategoryLayout();
            historyBox.setPrefHeight(230); // Taller height for vertical layout
        }
    }

    /** Add new server ID to history if not already present */
    public void addToHistory(String shortId) {
        String entry = "#" + shortId;
        if (!historyData.contains(entry)) {
            historyData.addFirst(entry);
        }
    }

    /** Reset the status panel for a new scan */
    public void reset() {
        // Show spinners for all fields
        ocrSpinner.setVisible(true);
        gtSpinner.setVisible(true);
        hackersSpinner.setVisible(true);
        verySusSpinner.setVisible(true);
        susSpinner.setVisible(true);
        legitSpinner.setVisible(true);

        // Reset server fields
        ocrValueLabel.setText(bundle.getString("server.hackers.waiting"));
        gtValueLabel.setText(bundle.getString("server.hackers.waiting"));

        // Reset player category values
        hackersValueLabel.setText("0");
        verySusValueLabel.setText("0");
        susValueLabel.setText("0");
        legitValueLabel.setText("0");
    }

    /**
     * Update the server number status
     */
    public void setOcrStatus(String text, boolean running) {
        ocrSpinner.setVisible(running);
        ocrValueLabel.setText(text);
    }

    /**
     * Update the long server number status
     */
    public void setGameToolsStatus(String text, boolean running) {
        gtSpinner.setVisible(running);
        gtValueLabel.setText(text);
    }

    /**
     * Update the player category counts with summary data
     */
    public void setHackersStatus(HackersSummary summary, boolean running) {
        // Hide spinners
        hackersSpinner.setVisible(running);
        verySusSpinner.setVisible(running);
        susSpinner.setVisible(running);
        legitSpinner.setVisible(running);

        // Update values
        hackersValueLabel.setText(String.valueOf(summary.numHackers()));
        verySusValueLabel.setText(String.valueOf(summary.numVerySus()));
        susValueLabel.setText(String.valueOf(summary.numSus()));
        legitValueLabel.setText(String.valueOf(summary.numLegit()));
    }

    /**
     * Legacy method for backward compatibility
     */
    public void setHackersStatus(String text, boolean running, boolean hasCheaters) {
        hackersSpinner.setVisible(running);
        verySusSpinner.setVisible(running);
        susSpinner.setVisible(running);
        legitSpinner.setVisible(running);

        // Extract just the number from text
        String numStr = text.replaceAll("[^0-9]", "");
        int hackers = 0;
        try {
            hackers = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            // Use default 0
        }

        hackersValueLabel.setText(String.valueOf(hackers));
        verySusValueLabel.setText("0");
        susValueLabel.setText("0");
        legitValueLabel.setText("0");
    }

    /**
     * Set the timer component to display scanning time
     */
    public void setTimeLabel(TimerComponent timeComponent) {
        this.timeComponent = timeComponent;
    }

    /**
     * Get the time component for use in other components
     */
    public TimerComponent getTimeLabel() {
        return timeComponent;
    }
}