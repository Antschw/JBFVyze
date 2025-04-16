package fr.antschw.bfv.domain.service;


import fr.antschw.bfv.domain.model.ServerInfo;

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
}
