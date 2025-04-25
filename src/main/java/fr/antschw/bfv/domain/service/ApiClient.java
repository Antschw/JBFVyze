package fr.antschw.bfv.domain.service;


import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;

/**
 * Defines the contract for API clients to fetch server information.
 */
public interface ApiClient {

    /**
     * Fetches the server information based on server ID.
     *
     * @param serverId the server identifier
     * @return ServerInfo object containing relevant server data
     * @throws ApiRequestException if the API request fails
     */
    ServerInfo fetchServerInfo(String serverId) throws ApiRequestException;

    default ServerPlayers fetchServerPlayers(String serverId) throws ApiRequestException {
        throw new UnsupportedOperationException("This API client does not support player fetching");
    }

    default UserStats fetchUserStats(String playerName) throws ApiRequestException {
        throw new UnsupportedOperationException("This API client does not support user stats fetching");
    }
}
