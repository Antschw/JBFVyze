package fr.antschw.bfv.application.orchestrator;

import com.google.inject.Inject;
import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.PlayerMonitoringService;
import fr.antschw.bfv.domain.service.SettingsService;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Coordinates monitoring a player's statistics over time.
 * Maintenant avec support pour la persistance des paramètres.
 */
public class PlayerMonitoringCoordinator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerMonitoringCoordinator.class);
    private static final int FETCH_INTERVAL_MINUTES = 2; // Intervalle fixe de 2 minutes

    private final PlayerMonitoringService monitoringService;
    private final SettingsService settingsService;

    @Inject
    public PlayerMonitoringCoordinator(
            PlayerMonitoringService monitoringService,
            SettingsService settingsService) {
        this.monitoringService = monitoringService;
        this.settingsService = settingsService;
        LOGGER.info("PlayerMonitoringCoordinator initialized");

        // Charger automatiquement le joueur sauvegardé s'il existe
        loadSavedPlayer();
    }

    /**
     * Charge et démarre le monitoring du joueur sauvegardé dans les paramètres.
     */
    private void loadSavedPlayer() {
        String playerName = settingsService.getPlayerName();
        if (playerName != null && !playerName.isEmpty()) {
            boolean isPlayerId = settingsService.isUsePlayerId();
            LOGGER.info("Loading saved player: {} (isPlayerId: {})", playerName, isPlayerId);

            // Démarrer le monitoring sans callback (sera géré par StatsView plus tard)
            startMonitoring(playerName, isPlayerId, null);
        }
    }

    /**
     * Starts monitoring a player.
     *
     * @param playerIdentifier the player name or ID
     * @param isPlayerId       true if the identifier is a player ID, false if it's a name
     * @param onStatsUpdated   callback when new stats are available (runs on JavaFX thread)
     */
    public void startMonitoring(String playerIdentifier, boolean isPlayerId, Consumer<UserStats> onStatsUpdated) {
        if (playerIdentifier == null || playerIdentifier.isEmpty()) {
            LOGGER.warn("Cannot start monitoring with empty player identifier");
            return;
        }

        LOGGER.info("Starting player monitoring for: {}", playerIdentifier);
        try {
            // Sauvegarder les paramètres
            settingsService.setPlayerName(playerIdentifier);
            settingsService.setUsePlayerId(isPlayerId);

            monitoringService.startMonitoring(playerIdentifier, isPlayerId,
                    stats -> {
                        if (stats != null) {
                            Platform.runLater(() -> {
                                try {
                                    if (onStatsUpdated != null) {
                                        onStatsUpdated.accept(stats);
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Error in stats update callback", e);
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Error starting player monitoring", e);
        }
    }

    /**
     * Stops the current monitoring.
     */
    public void stopMonitoring() {
        try {
            monitoringService.stopMonitoring();
        } catch (Exception e) {
            LOGGER.error("Error stopping monitoring", e);
        }
    }

    /**
     * Retrieves the current stats of the monitored player.
     *
     * @return the current stats or null if not monitoring
     */
    public UserStats getCurrentStats() {
        try {
            return monitoringService.getCurrentStats().orElse(null);
        } catch (Exception e) {
            LOGGER.error("Error getting current stats", e);
            return null;
        }
    }

    /**
     * Returns the player identifier (name or ID) being monitored.
     *
     * @return the player identifier or empty string if none
     */
    public String getMonitoredPlayer() {
        try {
            return monitoringService.getMonitoredPlayer().orElse("");
        } catch (Exception e) {
            LOGGER.error("Error getting monitored player", e);
            return "";
        }
    }

    /**
     * Retrieves all session snapshots.
     *
     * @return list of session stats
     */
    public List<SessionStats> getSessionHistory() {
        try {
            return monitoringService.getSessionHistory();
        } catch (Exception e) {
            LOGGER.error("Error retrieving session history", e);
            return Collections.emptyList();
        }
    }

    /**
     * Clears the current session data.
     */
    public void clearSession() {
        try {
            monitoringService.clearSessionHistory();
        } catch (Exception e) {
            LOGGER.error("Error clearing session", e);
        }
    }

    /**
     * Gets the timestamp when monitoring started.
     *
     * @return the start time or null if not monitoring
     */
    public Instant getSessionStartTime() {
        try {
            return monitoringService.getSessionStartTime();
        } catch (Exception e) {
            LOGGER.error("Error getting session start time", e);
            return null;
        }
    }

    /**
     * Retrieves the duration of the current session.
     *
     * @return formatted duration string (e.g., "1h 23m") or empty if not monitoring
     */
    public String getSessionDuration() {
        try {
            Instant startTime = getSessionStartTime();
            if (startTime == null) {
                return "";
            }

            Duration duration = Duration.between(startTime, Instant.now());
            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

            return hours > 0
                    ? String.format("%dh %02dm %02ds", hours, minutes, seconds)
                    : String.format("%dm %02ds", minutes, seconds);
        } catch (Exception e) {
            LOGGER.error("Error calculating session duration", e);
            return "";
        }
    }

    /**
     * Calculates session-specific metrics based on fixed intervals.
     * Les métriques sont maintenant calculées en utilisant l'intervalle fixe
     * de 2 minutes entre chaque échantillon.
     *
     * @return object containing session aggregate stats
     */
    public SessionMetrics calculateSessionMetrics() {
        try {
            List<SessionStats> history = getSessionHistory();
            if (history.isEmpty()) {
                return new SessionMetrics(0, 0, 0, "0%");
            }

            // First snapshot in session
            SessionStats first = history.get(0);
            // Latest snapshot
            SessionStats latest = history.get(history.size() - 1);

            // Calculate session-specific metrics
            int sessionKills = latest.kills() - first.kills();
            int sessionDeaths = latest.deaths() - first.deaths();
            double sessionKd = sessionDeaths > 0 ? (double) sessionKills / sessionDeaths : sessionKills;

            // Calculate session KPM based on fixed interval time
            // Basé sur le nombre de points moins 1 (car le premier point est à t=0)
            double sessionMinutes = (history.size() - 1) * FETCH_INTERVAL_MINUTES;
            double sessionKpm = sessionMinutes > 0
                    ? (double) sessionKills / sessionMinutes
                    : 0;

            // Pour le headshot rate, nous utilisons le taux global le plus récent
            String sessionHeadshots = latest.headshots();

            return new SessionMetrics(sessionKd, sessionKpm, sessionKills, sessionHeadshots);
        } catch (Exception e) {
            LOGGER.error("Error calculating session metrics", e);
            return new SessionMetrics(0, 0, 0, "0%");
        }
    }

    /**
     * Record class for session-specific metrics.
     */
    public record SessionMetrics(
            double killDeath,
            double killsPerMinute,
            int kills,
            String headshots) {
    }
}