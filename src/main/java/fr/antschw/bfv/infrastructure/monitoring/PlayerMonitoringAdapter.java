package fr.antschw.bfv.infrastructure.monitoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.PlayerMonitoringService;
import fr.antschw.bfv.domain.service.UserStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static fr.antschw.bfv.application.util.AppConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Implementation of a PlayerMonitoringService that fetches player stats at regular intervals.
 */
public class PlayerMonitoringAdapter implements PlayerMonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerMonitoringAdapter.class);
    private static final int UPDATE_INTERVAL_MINUTES = 5; // Modifié à 5 minutes

    private final UserStatsService userStatsService;
    // On n'initialise pas le scheduler ici pour éviter les problèmes de démarrage
    private ScheduledExecutorService scheduler;

    private String playerIdentifier;
    private boolean isPlayerId;
    private Consumer<UserStats> onStatsUpdated;
    private UserStats currentStats;
    private final List<SessionStats> sessionHistory = Collections.synchronizedList(new ArrayList<>());
    private Instant sessionStartTime;
    private long lastSecondsPlayed = -1;

    @Inject
    public PlayerMonitoringAdapter(@Named(GAMETOOLS_PLAYERS_NAME) UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
        LOGGER.info("PlayerMonitoringAdapter initialized with {} minute interval", UPDATE_INTERVAL_MINUTES);
    }

    @Override
    public void startMonitoring(String playerIdentifier, boolean isPlayerId, Consumer<UserStats> onStatsUpdated) {
        // Stop any existing monitoring
        stopMonitoring();

        // Initialiser le scheduler uniquement quand on commence le monitoring
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "player-monitoring-thread");
            t.setDaemon(true);
            return t;
        });

        this.playerIdentifier = playerIdentifier;
        this.isPlayerId = isPlayerId;
        this.onStatsUpdated = onStatsUpdated;
        this.sessionStartTime = Instant.now();
        this.sessionHistory.clear();
        this.lastSecondsPlayed = -1;

        // Fetch stats immediately
        fetchPlayerStats();

        // Schedule regular updates
        scheduler.scheduleAtFixedRate(
                this::fetchPlayerStats,
                UPDATE_INTERVAL_MINUTES,
                UPDATE_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );

        LOGGER.info("Started monitoring player: {} with {} minute interval", playerIdentifier, UPDATE_INTERVAL_MINUTES);
    }

    @Override
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            LOGGER.info("Stopped player monitoring");
        }
    }

    @Override
    public Optional<UserStats> getCurrentStats() {
        return Optional.ofNullable(currentStats);
    }

    @Override
    public List<SessionStats> getSessionHistory() {
        return Collections.unmodifiableList(sessionHistory);
    }

    @Override
    public void clearSessionHistory() {
        sessionHistory.clear();
        lastSecondsPlayed = -1;
    }

    @Override
    public Instant getSessionStartTime() {
        return sessionStartTime;
    }

    @Override
    public Optional<String> getMonitoredPlayer() {
        return playerIdentifier != null ? Optional.of(playerIdentifier) : Optional.empty();
    }

    /**
     * Fetches the latest stats for the monitored player.
     */
    private void fetchPlayerStats() {
        if (playerIdentifier == null || playerIdentifier.isBlank()) {
            LOGGER.warn("No player identifier set for monitoring");
            return;
        }

        try {
            LOGGER.debug("Fetching stats for player: {}", playerIdentifier);

            // This would need to be adapted to handle player IDs
            // For now, we use player name only
            UserStats stats = userStatsService.fetchUserStats(playerIdentifier);

            // Only update if stats have changed (seconds played increased)
            if (lastSecondsPlayed == -1 || stats.secondsPlayed() > lastSecondsPlayed) {
                lastSecondsPlayed = stats.secondsPlayed();
                currentStats = stats;

                // Create a session snapshot
                SessionStats snapshot = new SessionStats(
                        stats.username(),
                        Instant.now(),
                        stats.kills(),
                        stats.deaths(),
                        stats.killDeath(),
                        stats.killsPerMinute(),
                        stats.accuracy(),
                        stats.headshots(),
                        stats.secondsPlayed()
                );

                sessionHistory.add(snapshot);

                // Notify listeners
                if (onStatsUpdated != null) {
                    onStatsUpdated.accept(stats);
                }

                LOGGER.info("Updated stats for {}: K/D={}, KPM={}, Time={}",
                        stats.username(), stats.killDeath(), stats.killsPerMinute(), stats.timePlayed());
            } else {
                LOGGER.debug("No change in playtime for {}, stats not updated", stats.username());
            }

        } catch (ApiRequestException e) {
            LOGGER.error("Failed to fetch stats for {}: {}", playerIdentifier, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error monitoring player {}", playerIdentifier, e);
        }
    }
}