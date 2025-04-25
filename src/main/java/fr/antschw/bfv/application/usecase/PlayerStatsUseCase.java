package fr.antschw.bfv.application.usecase;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.antschw.bfv.application.port.UserStatsCachePort;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.domain.service.ServerPlayersClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsPlayerApiClient;

import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Use case for player statistics operations.
 */
public class PlayerStatsUseCase {

    private final GameToolsPlayerApiClient playerApiClient;
    private final UserStatsCachePort cache;

    /**
     * Constructor.
     */
    @Inject
    public PlayerStatsUseCase(@Named(GAMETOOLS_PLAYERS_NAME) ServerPlayersClient playerApiClient, UserStatsCachePort cache) {
        this.playerApiClient = (GameToolsPlayerApiClient) playerApiClient;
        this.cache = cache;
    }

    /**
     * Constructor with dependency injection for testing.
     *
     * @param playerApiClient custom player API client
     */
    public PlayerStatsUseCase(GameToolsPlayerApiClient playerApiClient, UserStatsCachePort cache) {
        this.playerApiClient = playerApiClient;
        this.cache = cache;
    }

    /**
     * Gets the players in a server.
     *
     * @param serverId server ID
     * @return server players information
     * @throws ApiRequestException if the API request fails
     */
    public ServerPlayers getServerPlayers(String serverId) throws ApiRequestException {
        return playerApiClient.fetchServerPlayers(serverId);
    }


    /**
     * Retrieves user stats, using cache when fresh.
     */
    public UserStats getPlayerStats(String playerName) {
        return cache.getCachedStats(playerName)
                .orElseGet(() -> {
                    UserStats fresh;
                    try {
                        fresh = playerApiClient.fetchUserStats(playerName);
                    } catch (ApiRequestException e) {
                        throw new RuntimeException(e);
                    }
                    cache.putStats(fresh);
                    return fresh;
                });
    }
}