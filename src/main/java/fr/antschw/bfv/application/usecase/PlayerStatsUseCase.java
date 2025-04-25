package fr.antschw.bfv.application.usecase;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.client.GameToolsPlayerApiClient;

import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Use case for player statistics operations.
 */
public class PlayerStatsUseCase {

    private final GameToolsPlayerApiClient playerApiClient;

    /**
     * Constructor.
     */
    @Inject
    public PlayerStatsUseCase(@Named(GAMETOOLS_PLAYERS_NAME) ApiClient playerApiClient) {
        this.playerApiClient = (GameToolsPlayerApiClient) playerApiClient;
    }

    /**
     * Constructor with dependency injection for testing.
     *
     * @param playerApiClient custom player API client
     */
    public PlayerStatsUseCase(GameToolsPlayerApiClient playerApiClient) {
        this.playerApiClient = playerApiClient;
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
     * Gets statistics for a player by name.
     *
     * @param playerName player name
     * @return player statistics
     * @throws ApiRequestException if the API request fails
     */
    public UserStats getPlayerStats(String playerName) throws ApiRequestException {
        return playerApiClient.fetchUserStats(playerName);
    }

}