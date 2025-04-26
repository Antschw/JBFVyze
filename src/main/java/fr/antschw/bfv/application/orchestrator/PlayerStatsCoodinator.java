package fr.antschw.bfv.application.orchestrator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.antschw.bfv.domain.service.UserStatsCacheService;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.domain.service.ServerPlayersService;
import fr.antschw.bfv.infrastructure.api.client.PlayerClient;

import static fr.antschw.bfv.application.util.constants.AppConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Use case for player statistics operations.
 */
public class PlayerStatsCoodinator {

    private final PlayerClient playerApiClient;
    private final UserStatsCacheService cache;

    /**
     * Constructor.
     */
    @Inject
    public PlayerStatsCoodinator(@Named(GAMETOOLS_PLAYERS_NAME) ServerPlayersService playerApiClient, UserStatsCacheService cache) {
        this.playerApiClient = (PlayerClient) playerApiClient;
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