package fr.antschw.bfv.infrastructure.api.client;

import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.infrastructure.api.model.ApiType;

/**
 * Factory for creating ApiClient instances based on API type.
 */
public class ApiClientFactory {

    private ApiClientFactory() {
        // Prevent instantiation
    }

    /**
     * Creates an ApiClient based on the specified ApiType.
     *
     * @param apiType the API type
     * @return ApiClient implementation
     */
    public static ApiClient createApiClient(ApiType apiType) {
        return switch (apiType) {
            case GAMETOOLS -> new GameToolsApiClient();
            case BFVHACKERS -> new BfvHackersApiClient();
            case GAMETOOLS_PLAYERS -> throw new IllegalArgumentException("For GameToolsPlayers, use createPlayerApiClient()");
        };
    }

    /**
     * Creates a GameToolsPlayerApiClient for player-specific operations.
     *
     * @return GameToolsPlayerApiClient instance
     */
    public static GameToolsPlayerApiClient createPlayerApiClient() {
        return new GameToolsPlayerApiClient();
    }
}