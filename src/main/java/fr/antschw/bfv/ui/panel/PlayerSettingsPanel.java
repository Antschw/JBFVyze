package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.application.orchestrator.PlayerMonitoringCoordinator;
import fr.antschw.bfv.application.util.I18nUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Panel for configuring player monitoring settings.
 */
public class PlayerSettingsPanel extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSettingsPanel.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final TextField playerIdentifierField = new TextField();
    private final CheckBox isPlayerIdCheckbox = new CheckBox();
    private final Button monitorButton = new Button();

    private final Label statusLabel = new Label();
    private final PlayerMonitoringCoordinator monitoringCoordinator;

    /**
     * Constructs the player settings panel.
     *
     * @param monitoringCoordinator the monitoring coordinator
     * @param onPlayerChanged callback when player changes
     */
    public PlayerSettingsPanel(
            PlayerMonitoringCoordinator monitoringCoordinator,
            BiConsumer<String, Boolean> onPlayerChanged) {

        LOGGER.info("Initializing PlayerSettingsPanel");
        this.monitoringCoordinator = monitoringCoordinator;
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getStyleClass().add("stats-settings-panel");

        try {
            // Header label
            Label headerLabel = new Label(bundle.getString("stats.settings.title"));
            headerLabel.getStyleClass().add("header-label");

            // Player identifier input
            Label playerLabel = new Label(bundle.getString("stats.settings.player"));
            playerLabel.getStyleClass().add("stats-label");

            playerIdentifierField.setPromptText(bundle.getString("stats.settings.player_prompt"));
            playerIdentifierField.setPrefWidth(200);

            isPlayerIdCheckbox.setText(bundle.getString("stats.settings.is_player_id"));
            isPlayerIdCheckbox.setSelected(false); // Default to player name

            monitorButton.setText(bundle.getString("stats.settings.monitor"));
            monitorButton.setOnAction(e -> {
                try {
                    String identifier = playerIdentifierField.getText().trim();
                    boolean isPlayerId = isPlayerIdCheckbox.isSelected();

                    if (identifier.isEmpty()) {
                        statusLabel.setText(bundle.getString("stats.settings.error_empty"));
                        statusLabel.getStyleClass().add("error-text");
                        return;
                    }

                    onPlayerChanged.accept(identifier, isPlayerId);
                    statusLabel.setText(bundle.getString("stats.settings.monitoring_started"));
                    statusLabel.getStyleClass().removeAll("error-text");
                    statusLabel.getStyleClass().add("success-text");
                } catch (Exception ex) {
                    LOGGER.error("Error while starting player monitoring", ex);
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.getStyleClass().add("error-text");
                }
            });

            // Status label
            statusLabel.getStyleClass().add("status-text");

            // Layout for player input
            HBox playerInputBox = new HBox(10);
            playerInputBox.setAlignment(Pos.CENTER_LEFT);
            playerInputBox.getChildren().addAll(playerLabel, playerIdentifierField, isPlayerIdCheckbox, monitorButton);

            // Add all to panel
            this.getChildren().addAll(headerLabel, playerInputBox, statusLabel);

            LOGGER.info("PlayerSettingsPanel initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing PlayerSettingsPanel", e);
            this.getChildren().clear();
            this.getChildren().add(new Label("Settings panel could not be initialized"));
        }
    }

    /**
     * Updates the panel with the currently monitored player.
     */
    public void updateMonitoredPlayer() {
        try {
            String currentPlayer = monitoringCoordinator.getMonitoredPlayer();
            if (currentPlayer != null && !currentPlayer.isEmpty()) {
                playerIdentifierField.setText(currentPlayer);
                statusLabel.setText(
                        bundle.getString("stats.settings.currently_monitoring") + " " + currentPlayer);
                statusLabel.getStyleClass().removeAll("error-text");
                statusLabel.getStyleClass().add("success-text");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating monitored player", e);
        }
    }
}