package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.model.ServerPlayers;

/**
 * Port for fetching the list of players on a server.
 */
public interface ServerPlayersClient {
    /**
     * Fetches all players on the given server.
     *
     * @param serverId the server short identifier
     * @return list of players wrapped in ServerPlayers
     * @throws ApiRequestException if the API request fails
     */
    ServerPlayers fetchServerPlayers(String serverId) throws ApiRequestException;
}
