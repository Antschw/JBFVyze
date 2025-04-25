package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.model.UserStats;

/**
 * Port for fetching detailed statistics of a single player.
 */
public interface UserStatsClient {
    /**
     * Fetches the stats for the given player name.
     *
     * @param playerName the player's username
     * @return populated UserStats
     * @throws ApiRequestException if the API request fails
     */
    UserStats fetchUserStats(String playerName) throws ApiRequestException;
}
