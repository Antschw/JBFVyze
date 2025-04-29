package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.application.util.I18nUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Panel displaying general player information and global stats.
 * Adapted to use horizontal layout to save vertical space.
 */
public class PlayerGeneralPanel extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerGeneralPanel.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final Label usernameLabel = new Label("--");
    private final Label playerIdLabel = new Label("--");
    private final Label rankLabel = new Label("--");
    private final Label kdLabel = new Label("--");
    private final Label kpmLabel = new Label("--");
    private final Label accuracyLabel = new Label("--");
    private final Label headshotsLabel = new Label("--");
    private final Label timePlayedLabel = new Label("--");

    // Flow pane qui s'adapte à la taille de la fenêtre
    private final FlowPane statsContainer = new FlowPane();

    public PlayerGeneralPanel() {
        LOGGER.info("Initializing PlayerGeneralPanel");
        this.setSpacing(5);
        this.setPadding(new Insets(5, 0, 5, 0));
        this.getStyleClass().add("stats-general-panel");

        try {
            // Header label
            Label headerLabel = new Label(bundle.getString("stats.general.title"));
            headerLabel.getStyleClass().add("header-label");

            // Configuration du FlowPane pour adaptation à la taille
            statsContainer.setHgap(15);
            statsContainer.setVgap(5);
            statsContainer.setPrefWrapLength(600);
            statsContainer.setPadding(new Insets(5, 0, 5, 0));

            // Ajouter les éléments horizontalement
            statsContainer.getChildren().addAll(
                    createStatBox("stats.general.username", usernameLabel),
                    createStatBox("stats.general.player_id", playerIdLabel),
                    createStatBox("stats.general.rank", rankLabel),
                    createStatBox("stats.general.kd", kdLabel),
                    createStatBox("stats.general.kpm", kpmLabel),
                    createStatBox("stats.general.accuracy", accuracyLabel),
                    createStatBox("stats.general.headshots", headshotsLabel),
                    createStatBox("stats.general.time_played", timePlayedLabel)
            );

            // Add all to panel
            this.getChildren().addAll(headerLabel, statsContainer);

            LOGGER.info("PlayerGeneralPanel initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing PlayerGeneralPanel", e);
            this.getChildren().clear();
            this.getChildren().add(new Label("General info panel could not be initialized"));
        }
    }

    /**
     * Helper to create a stat box with label and value.
     */
    private HBox createStatBox(String key, Label valueLabel) {
        try {
            Label label = new Label(bundle.getString(key) + ":");
            label.getStyleClass().add("stats-label-compact");

            valueLabel.getStyleClass().add("stats-value");

            HBox box = new HBox(5, label, valueLabel);
            box.setAlignment(Pos.CENTER_LEFT);
            box.getStyleClass().add("stat-box");

            return box;
        } catch (Exception e) {
            LOGGER.error("Error creating stat box for key: {}", key, e);
            return new HBox(new Label(key));
        }
    }

    /**
     * Updates the panel with the player's global stats.
     */
    public void updateStats(UserStats stats) {
        try {
            if (stats == null) {
                // Clear all values
                usernameLabel.setText("--");
                playerIdLabel.setText("--");
                rankLabel.setText("--");
                kdLabel.setText("--");
                kpmLabel.setText("--");
                accuracyLabel.setText("--");
                headshotsLabel.setText("--");
                timePlayedLabel.setText("--");
                return;
            }

            // Update values
            usernameLabel.setText(stats.username());
            playerIdLabel.setText(String.valueOf(stats.playerId()));
            rankLabel.setText(String.valueOf(stats.rank()));
            kdLabel.setText(String.format("%.2f", stats.killDeath()));
            kpmLabel.setText(String.format("%.2f", stats.killsPerMinute()));
            accuracyLabel.setText(stats.accuracy());
            headshotsLabel.setText(stats.headshots());
            timePlayedLabel.setText(stats.timePlayed());

            LOGGER.debug("Updated stats for player: {}", stats.username());
        } catch (Exception e) {
            LOGGER.error("Error updating player stats", e);
        }
    }
}