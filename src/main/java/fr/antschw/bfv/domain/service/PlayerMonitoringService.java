package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.domain.model.UserStats;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for monitoring a player's statistics over time.
 */
public interface PlayerMonitoringService {

    /**
     * Starts monitoring the specified player.
     *
     * @param playerIdentifier the player name or ID to monitor
     * @param isPlayerId       true if the identifier is a player ID, false if it's a name
     * @param onStatsUpdated   callback when new stats are available
     */
    void startMonitoring(String playerIdentifier, boolean isPlayerId, Consumer<UserStats> onStatsUpdated);

    /**
     * Stops the current monitoring.
     */
    void stopMonitoring();

    /**
     * Gets the player currently being monitored.
     *
     * @return the monitored player's latest stats or empty if none
     */
    Optional<UserStats> getCurrentStats();

    /**
     * Gets all session stats recorded since monitoring started.
     *
     * @return list of session stats snapshots
     */
    List<SessionStats> getSessionHistory();

    /**
     * Clears the current session history.
     */
    void clearSessionHistory();

    /**
     * Gets the timestamp when monitoring started.
     *
     * @return the start time or null if not monitoring
     */
    Instant getSessionStartTime();

    /**
     * Gets the player identifier (name or ID) currently being monitored.
     *
     * @return the player identifier or empty if none
     */
    Optional<String> getMonitoredPlayer();
}