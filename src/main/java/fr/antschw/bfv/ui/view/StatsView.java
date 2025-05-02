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

import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * View responsible for displaying player statistics and session monitoring.
 * Settings for player monitoring are now in SettingsView.
 * Modifié avec mise à jour automatique toutes les 12 minutes.
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

    // Intervalle de mise à jour fixe de 12 minutes
    private static final int FETCH_INTERVAL_MINUTES = 12;

    // Utilisation d'un scheduler unique pour toutes les mises à jour
    private ScheduledExecutorService uiUpdater;

    // Pour suivre si les stats ont changé
    private UserStats lastDisplayedStats = null;

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
        // Si un joueur est suivi, mais que la vue n'est pas à jour, forcer la mise à jour
        if (monitoringCoordinator.getCurrentStats() != null && lastDisplayedStats == null) {
            refreshView();
        }
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
     * Maintenant configuré pour rafraîchir les données toutes les 12 minutes exactement.
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

            // Mise à jour complète des stats toutes les 12 minutes
            uiUpdater.scheduleAtFixedRate(
                    () -> Platform.runLater(this::refreshView),
                    0, FETCH_INTERVAL_MINUTES, TimeUnit.MINUTES
            );

            LOGGER.info("UI updater started successfully with {} minute interval", FETCH_INTERVAL_MINUTES);
        } catch (Exception e) {
            LOGGER.error("Error starting UI updater", e);
        }
    }

    /**
     * Refreshes the entire view.
     * Mise à jour indépendante des changements des stats.
     */
    private void refreshView() {
        try {
            LOGGER.debug("Refreshing view with updated stats");

            // Update header text with current player
            updateHeaderLabel();

            // Get the latest data
            UserStats currentStats = monitoringCoordinator.getCurrentStats();
            UserStats initialStats = monitoringCoordinator.getInitialStats();

            // Skip if no data
            if (currentStats == null) {
                return;
            }

            // Update the general panel
            generalPanel.updateStats(currentStats);
            lastDisplayedStats = currentStats;

            // Update session-specific panels if a session is active
            if (monitoringCoordinator.getSessionStartTime() != null) {
                // Définir/mettre à jour le temps de départ pour le timer
                Instant startTime = monitoringCoordinator.getSessionStartTime();
                summaryPanel.setSessionStartTime(startTime);

                // Update initial stats for trend icons
                if (initialStats != null) {
                    summaryPanel.setInitialStats(initialStats);

                    // Mettre à jour aussi les stats initiales dans le chart panel
                    chartPanel.setInitialStats(initialStats);
                }

                // Get session history
                List<SessionStats> history = monitoringCoordinator.getSessionHistory();

                // Update chart avec l'historique mis à jour
                if (!history.isEmpty()) {
                    chartPanel.setSessionStartTime(startTime);
                    chartPanel.updateChart(history);
                }

                // Calculate and update session metrics
                PlayerMonitoringCoordinator.SessionMetrics metrics =
                        monitoringCoordinator.calculateSessionMetrics();
                summaryPanel.updateSessionMetrics(metrics);
            }
        } catch (Exception e) {
            LOGGER.error("Error refreshing view", e);
        }
    }

    /**
     * Called from SettingsView when player settings change.
     * Should be called by SettingsView after player monitoring is started.
     */
    public void refreshOnPlayerChange() {
        try {
            startUiUpdates(); // Restart UI updates

            // Update initial stats for trend calculation
            UserStats currentStats = monitoringCoordinator.getCurrentStats();
            UserStats initialStats = monitoringCoordinator.getInitialStats();

            if (initialStats != null) {
                chartPanel.setInitialStats(initialStats);
                summaryPanel.setInitialStats(initialStats);
            }

            if (currentStats != null) {
                lastDisplayedStats = currentStats;
            }

            // Mettre à jour l'affichage complet
            refreshView();
        } catch (Exception e) {
            LOGGER.error("Error refreshing on player change", e);
        }
    }
}