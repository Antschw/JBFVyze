package fr.antschw.bfv.infrastructure.monitoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.PlayerMonitoringService;
import fr.antschw.bfv.domain.service.SettingsService;
import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.domain.service.UserStatsService;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static fr.antschw.bfv.application.util.AppConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Implementation of a PlayerMonitoringService that fetches player stats at regular intervals.
 * Modifié pour utiliser un intervalle de 12 minutes et sauvegarder les settings.
 */
public class PlayerMonitoringAdapter implements PlayerMonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerMonitoringAdapter.class);
    private static final int UPDATE_INTERVAL_MINUTES = 12; // Modifié à 12 minutes

    private final UserStatsService userStatsService;
    private final SettingsService settingsService;

    // On n'initialise pas le scheduler ici pour éviter les problèmes de démarrage
    private ScheduledExecutorService scheduler;

    private String playerIdentifier;
    private boolean isPlayerId;
    private Consumer<UserStats> onStatsUpdated;
    private UserStats currentStats;
    private UserStats initialStats; // Stats du début de session
    private final List<SessionStats> sessionHistory = Collections.synchronizedList(new ArrayList<>());
    private Instant sessionStartTime;
    private long lastSecondsPlayed = -1;

    @Inject
    public PlayerMonitoringAdapter(
            @Named(GAMETOOLS_PLAYERS_NAME) UserStatsService userStatsService,
            SettingsService settingsService) {
        this.userStatsService = userStatsService;
        this.settingsService = settingsService;

        // Initialiser avec les valeurs sauvegardées
        this.playerIdentifier = settingsService.getPlayerName();
        this.isPlayerId = settingsService.isUsePlayerId();

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
        this.initialStats = null; // Réinitialiser les stats initiales

        // Fetch stats immediately
        fetchPlayerStats();

        // Schedule regular updates every 12 minutes
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
    public Optional<UserStats> getInitialStats() {
        return Optional.ofNullable(initialStats);
    }

    @Override
    public List<SessionStats> getSessionHistory() {
        return Collections.unmodifiableList(sessionHistory);
    }

    @Override
    public void clearSessionHistory() {
        sessionHistory.clear();
        lastSecondsPlayed = -1;
        initialStats = null;
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
     * Maintenant, un nouvel échantillon est systématiquement ajouté
     * toutes les 12 minutes, qu'il y ait ou non un changement dans
     * les statistiques de jeu.
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
            currentStats = stats;

            // Si c'est la première fois qu'on récupère des stats, on les sauvegarde comme stats initiales
            if (initialStats == null) {
                initialStats = stats;
                LOGGER.info("Initial stats saved for {}: kills={}, deaths={}, Time={}",
                        stats.username(), stats.kills(), stats.deaths(), stats.secondsPlayed());
            }

            // Create a session snapshot with current timestamp
            // Cela garantit un échantillon toutes les 12 minutes exactement
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

            // Toujours ajouter le nouvel échantillon
            sessionHistory.add(snapshot);
            lastSecondsPlayed = stats.secondsPlayed();

            // Notify listeners
            if (onStatsUpdated != null) {
                onStatsUpdated.accept(stats);
            }

            LOGGER.info("Updated stats for {}: kills={}, deaths={}, Time={}",
                    stats.username(), stats.kills(), stats.deaths(), stats.secondsPlayed());

        } catch (ApiRequestException e) {
            LOGGER.error("Failed to fetch stats for {}: {}", playerIdentifier, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error monitoring player {}", playerIdentifier, e);
        }
    }
}