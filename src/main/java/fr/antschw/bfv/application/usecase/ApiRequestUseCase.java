package fr.antschw.bfv.application.usecase;

import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.domain.service.ApiRequestException;
import com.google.inject.Inject;

/**
 * Use case class to orchestrate API requests.
 */
public class ApiRequestUseCase {

    private final ApiClient apiClient;

    /**
     * Constructor with dependency injection.
     *
     * @param apiClient the API client to use
     */
    @Inject
    public ApiRequestUseCase(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fetches server information.
     *
     * @param serverId the server identifier
     * @return ServerInfo object
     * @throws ApiRequestException if the request fails
     */
    public ServerInfo getServerInfo(String serverId) throws ApiRequestException {
        return apiClient.fetchServerInfo(serverId);
    }
}
