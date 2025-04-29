package fr.antschw.bfv.ui.view;

import com.google.inject.Inject;
import fr.antschw.bfv.application.orchestrator.PlayerMonitoringCoordinator;
import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.ui.panel.PlayerChartPanel;
import fr.antschw.bfv.ui.panel.PlayerGeneralPanel;
import fr.antschw.bfv.ui.panel.SessionSummaryPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * View responsible for displaying player statistics and session monitoring.
 * Settings for player monitoring are now in SettingsView.
 */
public class StatsView {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsView.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final VBox root = new VBox(10);
    private final PlayerMonitoringCoordinator monitoringCoordinator;

    private final Label headerLabel = new Label();
    private final PlayerGeneralPanel generalPanel;
    private final PlayerChartPanel chartPanel;
    private final SessionSummaryPanel summaryPanel;

    // Lazy initialization du scheduler avec un intervalle plus long
    private ScheduledExecutorService uiUpdater;
    private static final int UI_UPDATE_INTERVAL_SECONDS = 60; // 60s pour l'UI

    // Pour suivre si les stats ont changé
    private UserStats lastDisplayedStats = null;
    private boolean dataUpdated = false;

    /**
     * Constructs the StatsView.
     */
    @Inject
    public StatsView(PlayerMonitoringCoordinator monitoringCoordinator) {
        LOGGER.info("Initializing StatsView");
        this.monitoringCoordinator = monitoringCoordinator;

        // Initialize all panels
        this.generalPanel = new PlayerGeneralPanel();
        this.chartPanel = new PlayerChartPanel();
        this.summaryPanel = new SessionSummaryPanel();

        try {
            // Configure the root layout
            root.setPadding(new Insets(20));

            // Header label - initialisation pour éviter NPE
            headerLabel.setText(bundle.getString("stats.title").replace("{0}", ""));
            headerLabel.getStyleClass().add("header-label");

            // Create separators
            Separator separator1 = new Separator();
            Separator separator2 = new Separator();

            // Add all panels - sans PlayerSettingsPanel
            root.getChildren().addAll(
                    headerLabel,
                    generalPanel,
                    separator1,
                    chartPanel,
                    separator2,
                    summaryPanel
            );

            // Set growth behaviors - graphique plus grand
            VBox.setVgrow(chartPanel, Priority.ALWAYS);

            // Start UI updates immediately to show current player if any
            startUiUpdates();

            // Update header with current player
            updateHeaderLabel();

            LOGGER.info("StatsView initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing StatsView layout", e);
            // Fallback to a simple message on error
            root.getChildren().clear();
            root.getChildren().add(new Label("Error initializing Stats view: " + e.getMessage()));
        }
    }

    /**
     * Returns the root node for this view.
     */
    public VBox getView() {
        return root;
    }

    /**
     * Updates the header label with the player name.
     */
    private void updateHeaderLabel() {
        try {
            String player = monitoringCoordinator.getMonitoredPlayer();
            if (player == null || player.isEmpty()) {
                headerLabel.setText(bundle.getString("stats.title").replace("{0}", ""));
            } else {
                headerLabel.setText(bundle.getString("stats.title").replace("{0}", player));
            }
        } catch (Exception e) {
            LOGGER.error("Error updating header label", e);
        }
    }

    /**
     * Starts the UI update scheduler.
     */
    private void startUiUpdates() {
        try {
            LOGGER.info("Starting UI updater");

            if (uiUpdater != null && !uiUpdater.isShutdown()) {
                uiUpdater.shutdown();
            }

            // Create a new scheduler
            uiUpdater = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "stats-ui-updater");
                t.setDaemon(true);
                return t;
            });

            // Update the session duration every second
            uiUpdater.scheduleAtFixedRate(() -> {
                try {
                    if (monitoringCoordinator.getSessionStartTime() != null) {
                        Platform.runLater(() -> {
                            try {
                                String duration = monitoringCoordinator.getSessionDuration();
                                summaryPanel.setSessionDuration(duration);
                            } catch (Exception e) {
                                LOGGER.debug("Error updating session duration", e);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error("Error in session duration scheduler", e);
                }
            }, 0, 1, TimeUnit.SECONDS);

            // Update the stats display every minute, mais juste si les données ont changé
            uiUpdater.scheduleAtFixedRate(() -> {
                try {
                    UserStats currentStats = monitoringCoordinator.getCurrentStats();
                    if (currentStats != null &&
                            (lastDisplayedStats == null || !currentStats.equals(lastDisplayedStats) || dataUpdated)) {
                        refreshView();
                        lastDisplayedStats = currentStats;
                        dataUpdated = false;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error checking for stats updates", e);
                }
            }, 0, UI_UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);

            LOGGER.info("UI updater started successfully");
        } catch (Exception e) {
            LOGGER.error("Error starting UI updater", e);
        }
    }

    /**
     * Refreshes the entire view.
     */
    private void refreshView() {
        Platform.runLater(() -> {
            try {
                LOGGER.debug("Refreshing view with updated stats");

                // Update header text with current player
                updateHeaderLabel();

                // Get the latest data
                UserStats currentStats = monitoringCoordinator.getCurrentStats();

                // Skip if no data
                if (currentStats == null) {
                    return;
                }

                // Update the general panel
                generalPanel.updateStats(currentStats);

                // Update session-specific panels if a session is active
                if (monitoringCoordinator.getSessionStartTime() != null) {
                    // Get session history
                    List<SessionStats> history = monitoringCoordinator.getSessionHistory();

                    // Update chart - seulement si l'historique a changé
                    if (!history.isEmpty()) {
                        chartPanel.setSessionStartTime(monitoringCoordinator.getSessionStartTime());
                        chartPanel.updateChart(history);
                    }

                    // Calculate and update session metrics
                    PlayerMonitoringCoordinator.SessionMetrics metrics =
                            monitoringCoordinator.calculateSessionMetrics();
                    summaryPanel.updateSessionMetrics(metrics);
                }
            } catch (Exception e) {
                LOGGER.debug("Error refreshing view", e);
            }
        });
    }

    /**
     * Called from SettingsView when player settings change.
     * Should be called by SettingsView after player monitoring is started.
     */
    public void refreshOnPlayerChange() {
        try {
            startUiUpdates(); // Restart UI updates
            updateHeaderLabel();
            dataUpdated = true; // Forcer un rafraîchissement

            // Set latest stats as initial for trend calculation
            UserStats currentStats = monitoringCoordinator.getCurrentStats();
            if (currentStats != null) {
                summaryPanel.setInitialStats(currentStats);
            }
        } catch (Exception e) {
            LOGGER.error("Error refreshing on player change", e);
        }
    }

    /**
     * Force l'actualisation des données
     */
    public void notifyDataUpdated() {
        dataUpdated = true;
    }
}