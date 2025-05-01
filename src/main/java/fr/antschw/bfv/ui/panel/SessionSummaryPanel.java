package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.application.orchestrator.PlayerMonitoringCoordinator;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.ui.component.TimerComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ResourceBundle;

/**
 * Panel displaying summary statistics for the current session.
 * Adapted to use TimerComponent and calculate session metrics using fixed intervals.
 */
public class SessionSummaryPanel extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionSummaryPanel.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final TimerComponent sessionTimer = new TimerComponent();
    private final Label sessionKdLabel = new Label("--");
    private final Label sessionKdTrendIcon = createTrendIcon();
    private final Label sessionKpmLabel = new Label("--");
    private final Label sessionKpmTrendIcon = createTrendIcon();
    private final Label sessionHeadshotsLabel = new Label("--");
    private final Label sessionHeadshotsTrendIcon = createTrendIcon();
    private final Label sessionKillsLabel = new Label("--");

    // FlowPane pour adapter le contenu horizontalement
    private final FlowPane statsContainer = new FlowPane();

    // Initial values to compare against for trend calculation
    private double initialKd = 0;
    private double initialKpm = 0;
    private double initialHeadshots = 0;

    private boolean hasInitialStats = false;

    public SessionSummaryPanel() {
        LOGGER.info("Initializing SessionSummaryPanel");
        this.setSpacing(5);
        this.setPadding(new Insets(5, 0, 5, 0));
        this.getStyleClass().add("stats-summary-panel");

        try {
            // Header avec timer intégré
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label headerLabel = new Label(bundle.getString("stats.session.title"));
            headerLabel.getStyleClass().add("header-label");

            // Ajouter un spacer qui pousse le timer à droite
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            headerBox.getChildren().addAll(headerLabel, spacer, sessionTimer);

            // Configuration du FlowPane
            statsContainer.setHgap(15);
            statsContainer.setVgap(5);
            statsContainer.setPrefWrapLength(600);
            statsContainer.setPadding(new Insets(5, 0, 5, 0));

            // Ajouter chaque stat box horizontalement
            statsContainer.getChildren().addAll(
                    createStatBox("stats.session.kills", sessionKillsLabel, null),
                    createStatBox("stats.session.kd", sessionKdLabel, sessionKdTrendIcon),
                    createStatBox("stats.session.kpm", sessionKpmLabel, sessionKpmTrendIcon),
                    createStatBox("stats.session.headshots", sessionHeadshotsLabel, sessionHeadshotsTrendIcon)
            );

            // Add all to panel
            this.getChildren().addAll(headerBox, statsContainer);

            LOGGER.info("SessionSummaryPanel initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing SessionSummaryPanel", e);
            this.getChildren().clear();
            this.getChildren().add(new Label("Session summary could not be initialized"));
        }
    }

    /**
     * Helper to create a stat box with label, value and optional trend icon.
     */
    private HBox createStatBox(String key, Label valueLabel, Label trendIconLabel) {
        try {
            Label label = new Label(bundle.getString(key) + ":");
            label.getStyleClass().add("stats-label-compact");

            valueLabel.getStyleClass().add("stats-value");

            HBox box;
            if (trendIconLabel != null) {
                box = new HBox(5, label, valueLabel, trendIconLabel);
            } else {
                box = new HBox(5, label, valueLabel);
            }

            box.setAlignment(Pos.CENTER_LEFT);
            box.getStyleClass().add("stat-box");

            return box;
        } catch (Exception e) {
            LOGGER.error("Error creating stat box for key: {}", key, e);
            return new HBox(new Label(key));
        }
    }

    /**
     * Helper to create a trend icon.
     */
    private Label createTrendIcon() {
        try {
            FontIcon icon = new FontIcon("mdi2e-equal");
            icon.setIconSize(16);
            Label label = new Label();
            label.setGraphic(icon);
            return label;
        } catch (Exception e) {
            LOGGER.error("Error creating trend icon", e);
            return new Label("-");
        }
    }

    /**
     * Sets the session start time and starts the timer.
     */
    public void setSessionStartTime(Instant startTime) {
        try {
            if (startTime != null) {
                sessionTimer.start(startTime);
            } else {
                sessionTimer.reset();
            }
        } catch (Exception e) {
            LOGGER.error("Error setting session start time", e);
        }
    }

    /**
     * Updates the panel with initial stats.
     */
    public void setInitialStats(UserStats stats) {
        try {
            if (stats == null) {
                hasInitialStats = false;
                return;
            }

            initialKd = stats.killDeath();
            initialKpm = stats.killsPerMinute();
            initialHeadshots = parsePercentage(stats.headshots());
            hasInitialStats = true;

            LOGGER.debug("Initial stats set: KD={}, KPM={}, Headshots={}%",
                    initialKd, initialKpm, initialHeadshots);
        } catch (Exception e) {
            LOGGER.error("Error setting initial stats", e);
            hasInitialStats = false;
        }
    }

    /**
     * Formats a headshot percentage string (e.g., "20.5%") as a double.
     */
    private double parsePercentage(String percentage) {
        try {
            // Remove % symbol and parse as double
            return Double.parseDouble(percentage.replace("%", ""));
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.debug("Error parsing percentage: {}", percentage);
            return 0.0;
        }
    }

    /**
     * Updates the panel with session metrics.
     */
    public void updateSessionMetrics(PlayerMonitoringCoordinator.SessionMetrics metrics) {
        try {
            if (metrics == null) {
                return;
            }

            // Update labels with session metrics
            sessionKdLabel.setText(String.format("%.2f", metrics.killDeath()));
            sessionKpmLabel.setText(String.format("%.2f", metrics.killsPerMinute()));
            sessionHeadshotsLabel.setText(metrics.headshots());
            sessionKillsLabel.setText(String.valueOf(metrics.kills()));

            // Update trend icons if we have initial stats to compare against
            if (hasInitialStats) {
                updateTrendIcon(sessionKdTrendIcon, metrics.killDeath(), initialKd);
                updateTrendIcon(sessionKpmTrendIcon, metrics.killsPerMinute(), initialKpm);
                updateTrendIcon(sessionHeadshotsTrendIcon, parsePercentage(metrics.headshots()), initialHeadshots);
            }
        } catch (Exception e) {
            LOGGER.error("Error updating session metrics", e);
        }
    }

    /**
     * Updates a trend icon based on current vs initial value.
     */
    private void updateTrendIcon(Label iconLabel, double current, double initial) {
        try {
            FontIcon icon;

            double diff = current - initial;
            double threshold = 0.01; // Small threshold to avoid noise

            if (diff > threshold) {
                // Increasing (green up arrow)
                icon = new FontIcon("mdi2a-arrow-up");
                icon.getStyleClass().add("trend-up");
            } else if (diff < -threshold) {
                // Decreasing (red down arrow)
                icon = new FontIcon("mdi2a-arrow-down");
                icon.getStyleClass().add("trend-down");
            } else {
                // Stable (yellow equals sign)
                icon = new FontIcon("mdi2e-equal");
                icon.getStyleClass().add("trend-stable");
            }

            icon.setIconSize(16);
            iconLabel.setGraphic(icon);
        } catch (Exception e) {
            LOGGER.debug("Error updating trend icon", e);
            // Fallback to simple equals sign
            try {
                FontIcon icon = new FontIcon("mdi2e-equal");
                icon.setIconSize(16);
                iconLabel.setGraphic(icon);
            } catch (Exception ex) {
                // If even the fallback fails, do nothing
            }
        }
    }

    /**
     * Retourne le composant timer pour l'utiliser dans d'autres vues si nécessaire.
     */
    public TimerComponent getSessionTimer() {
        return sessionTimer;
    }
}