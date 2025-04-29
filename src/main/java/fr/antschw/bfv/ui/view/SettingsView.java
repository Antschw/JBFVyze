package fr.antschw.bfv.ui.view;

import com.google.inject.Inject;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.application.orchestrator.PlayerMonitoringCoordinator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * JavaFX view for configuring the scan hotkey and player monitoring settings.
 */
public class SettingsView {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsView.class);
    private final VBox view = new VBox(15);
    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final HotkeyConfigurationService hotkeyConfigurationService;
    private final PlayerMonitoringCoordinator monitoringCoordinator;
    private final StatsView statsView;

    // Hotkey section
    private final Label currentKeyLabel = new Label();
    private final TextField keyCaptureField = new TextField();
    private final Button saveHotkeyButton = new Button();

    // Player monitoring section
    private final TextField playerNameField = new TextField();
    private final CheckBox usePlayerIdCheck = new CheckBox();
    private final Button savePlayerButton = new Button();
    private final Label playerStatusLabel = new Label();

    /**
     * Constructs the SettingsView with injected services.
     */
    @Inject
    public SettingsView(
            HotkeyConfigurationService hotkeyConfigurationService,
            PlayerMonitoringCoordinator monitoringCoordinator,
            StatsView statsView) {
        this.hotkeyConfigurationService = hotkeyConfigurationService;
        this.monitoringCoordinator = monitoringCoordinator;
        this.statsView = statsView;
        initLayout();
    }

    private void initLayout() {
        try {
            LOGGER.info("Initializing SettingsView");
            view.setPadding(new Insets(20));

            // Page title
            Label title = new Label(bundle.getString("settings.title"));
            title.getStyleClass().add("header-label");

            // Hotkey Section
            Label hotkeyTitle = new Label(bundle.getString("settings.screenshot"));
            hotkeyTitle.getStyleClass().add("section-title");

            updateCurrentKeyLabel();

            keyCaptureField.setPromptText(bundle.getString("settings.capture.prompt"));
            keyCaptureField.setEditable(false);
            keyCaptureField.setFocusTraversable(true);
            keyCaptureField.getStyleClass().add("settings-field");

            keyCaptureField.setOnKeyPressed(this::handleKeyPressed);

            saveHotkeyButton.setText(bundle.getString("settings.save"));
            saveHotkeyButton.setDisable(true);
            saveHotkeyButton.setOnAction(e -> saveHotkey());

            // Separator
            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));

            // Player Monitoring Section
            Label playerTitle = new Label(bundle.getString("settings.stats"));
            playerTitle.getStyleClass().add("section-title");

            // Initialize player name field
            String currentPlayer = monitoringCoordinator.getMonitoredPlayer();
            playerNameField.setText(currentPlayer);
            playerNameField.setPromptText(bundle.getString("stats.settings.player_prompt"));
            playerNameField.getStyleClass().add("settings-field");
            playerNameField.setPrefWidth(200);

            // Player ID switch (using custom styled checkbox instead of ToggleSwitch)
            usePlayerIdCheck.setText(bundle.getString("stats.settings.is_player_id"));
            usePlayerIdCheck.setSelected(false); // Default to player name
            usePlayerIdCheck.getStyleClass().add("switch-checkbox");

            // Save button
            savePlayerButton.setText(bundle.getString("settings.save"));
            savePlayerButton.setOnAction(e -> savePlayerSettings());

            // Status label
            playerStatusLabel.getStyleClass().add("status-text");
            if (!currentPlayer.isEmpty()) {
                playerStatusLabel.setText(bundle.getString("stats.settings.currently_monitoring") + " " + currentPlayer);
                playerStatusLabel.getStyleClass().add("success-text");
            }

            // Layout for hotkey section
            VBox hotkeySection = new VBox(5);
            hotkeySection.getChildren().addAll(
                    hotkeyTitle,
                    currentKeyLabel,
                    new HBox(10, keyCaptureField, saveHotkeyButton)
            );

            // Layout for player monitoring section
            HBox playerInputBox = new HBox(10);
            playerInputBox.setAlignment(Pos.CENTER_LEFT);
            playerInputBox.getChildren().addAll(playerNameField, usePlayerIdCheck, savePlayerButton);

            VBox playerSection = new VBox(5);
            playerSection.getChildren().addAll(
                    playerTitle,
                    playerInputBox,
                    playerStatusLabel
            );

            // Add all to main view
            view.getChildren().addAll(
                    title,
                    hotkeySection,
                    separator,
                    playerSection
            );

            LOGGER.info("SettingsView initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing SettingsView", e);
            view.getChildren().clear();
            view.getChildren().add(new Label("Settings view could not be initialized: " + e.getMessage()));
        }
    }

    private void updateCurrentKeyLabel() {
        try {
            String hotkey = hotkeyConfigurationService.getConfiguration().getHotkey();
            currentKeyLabel.setText(bundle.getString("settings.current").replace("{0}", hotkey));
            currentKeyLabel.getStyleClass().add("settings-value");
        } catch (Exception e) {
            LOGGER.error("Error updating current key label", e);
            currentKeyLabel.setText("Current hotkey: error");
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        try {
            KeyCode code = event.getCode();
            if (code != null) {
                keyCaptureField.setText(code.getName());
                saveHotkeyButton.setDisable(false);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling key press", e);
        }
    }

    private void saveHotkey() {
        try {
            String newHotkey = keyCaptureField.getText();
            hotkeyConfigurationService.updateConfiguration(newHotkey);
            updateCurrentKeyLabel();
            saveHotkeyButton.setDisable(true);

            // Show success message
            playerStatusLabel.setText(bundle.getString("settings.saved"));
            playerStatusLabel.getStyleClass().removeAll("error-text");
            playerStatusLabel.getStyleClass().add("success-text");

            LOGGER.info("Hotkey saved: {}", newHotkey);
        } catch (Exception e) {
            LOGGER.error("Error saving hotkey", e);
            playerStatusLabel.setText("Error: " + e.getMessage());
            playerStatusLabel.getStyleClass().add("error-text");
        }
    }

    private void savePlayerSettings() {
        try {
            String playerIdentifier = playerNameField.getText().trim();
            boolean isPlayerId = usePlayerIdCheck.isSelected();

            if (playerIdentifier.isEmpty()) {
                playerStatusLabel.setText(bundle.getString("stats.settings.error_empty"));
                playerStatusLabel.getStyleClass().removeAll("success-text");
                playerStatusLabel.getStyleClass().add("error-text");
                return;
            }

            // Start monitoring this player
            monitoringCoordinator.startMonitoring(playerIdentifier, isPlayerId, stats -> {
                // This callback will run when stats are updated
            });

            // Notify StatsView to refresh
            statsView.refreshOnPlayerChange();

            // Show success message
            playerStatusLabel.setText(bundle.getString("stats.settings.monitoring_started"));
            playerStatusLabel.getStyleClass().removeAll("error-text");
            playerStatusLabel.getStyleClass().add("success-text");

            LOGGER.info("Started monitoring player: {}", playerIdentifier);
        } catch (Exception e) {
            LOGGER.error("Error saving player settings", e);
            playerStatusLabel.setText("Error: " + e.getMessage());
            playerStatusLabel.getStyleClass().add("error-text");
        }
    }

    /**
     * Returns the root node for this view.
     *
     * @return VBox view
     */
    public VBox getView() {
        return view;
    }

    /**
     * Creates a simple custom switch UI using standard JavaFX components.
     * This is a simpler approach than the ToggleSwitch class.
     */
    private HBox createSwitch(String text, boolean initialState) {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        // Background rectangle
        Rectangle background = new Rectangle(40, 20);
        background.setArcHeight(20);
        background.setArcWidth(20);

        // Thumb circle
        Circle thumb = new Circle(10);
        thumb.setFill(Color.WHITE);

        // Initial state styling
        updateSwitchStyle(background, thumb, initialState);

        // Container for the switch visuals
        StackPane switchNode = new StackPane();
        switchNode.getChildren().addAll(background, thumb);

        // Position the thumb based on state
        thumb.setTranslateX(initialState ? 10 : -10);

        // Label
        Label label = new Label(text);

        // Click handling
        switchNode.setOnMouseClicked(e -> {
            boolean newState = !isThumbActive(thumb);
            thumb.setTranslateX(newState ? 10 : -10);
            updateSwitchStyle(background, thumb, newState);
        });

        container.getChildren().addAll(switchNode, label);
        return container;
    }

    /**
     * Gets the state of the switch based on thumb position.
     */
    private boolean isThumbActive(Circle thumb) {
        return thumb.getTranslateX() > 0;
    }

    /**
     * Updates switch styling based on state.
     */
    private void updateSwitchStyle(Rectangle background, Circle thumb, boolean active) {
        if (active) {
            background.setFill(Color.valueOf("#4CAF50"));
        } else {
            background.setFill(Color.valueOf("#CCCCCC"));
        }
    }
}