package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.domain.model.ServerInfo;

/**
 * Port for fetching basic server information (name, IDs, cheater count).
 */
public interface ServerInfoService {
    /**
     * Fetches server information for the given server identifier.
     *
     * @param serverId the server identifier (short or long form)
     * @return populated ServerInfo
     * @throws ApiRequestException if the API request fails
     */
    ServerInfo fetchServerInfo(String serverId) throws ApiRequestException;
}
