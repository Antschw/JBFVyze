package fr.antschw.bfv.infrastructure.api.client;

import fr.antschw.bfv.common.constants.ApiConstants;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Implementation of ApiClient for GameTools API.
 */
public class GameToolsApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameToolsApiClient.class);
    private final HttpClient httpClient;

    public GameToolsApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                .build();
    }

    @Override
    public ServerInfo fetchServerInfo(String shortServerId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsUri(shortServerId);
            LOGGER.debug("Requesting GameTools API with URI: {}", uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("GameTools API returned status code: {}", response.statusCode());
                throw new ApiRequestException("GameTools API returned non-200 status code: " + response.statusCode());
            }

            LOGGER.debug("GameTools API response: {}", response.body());
            JSONObject json = new JSONObject(response.body());
            JSONArray servers = json.optJSONArray(ApiConstants.JSON_SERVERS);

            if (servers == null || servers.isEmpty()) {
                LOGGER.warn("No servers found in API response for short ID: {}", shortServerId);
                throw new ApiRequestException("No servers found for short ID: " + shortServerId);
            }

            for (int i = 0; i < servers.length(); i++) {
                JSONObject server = servers.getJSONObject(i);
                String prefix = server.optString(ApiConstants.JSON_PREFIX, "");
                LOGGER.debug("Checking server prefix: {}", prefix);

                if (prefix.contains("#" + shortServerId)) {
                    String gameId = server.optString(ApiConstants.JSON_GAMEID);
                    LOGGER.info("Found matching server with gameId: {}", gameId);

                    try {
                        long longId = Long.parseLong(gameId);
                        return new ServerInfo(
                                "Server #" + shortServerId,
                                shortServerId,
                                longId,
                                0
                        );
                    } catch (NumberFormatException e) {
                        LOGGER.error("Failed to parse gameId as long: {}", gameId, e);
                        throw new ApiRequestException("Invalid gameId format: " + gameId, e);
                    }
                }
            }

            LOGGER.warn("No matching server found for short ID: {}", shortServerId);
            throw new ApiRequestException("No matching server found for short ID: " + shortServerId);

        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI for GameTools API", e);
            throw new ApiRequestException("Invalid URI for GameTools API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Request interrupted while fetching server info", e);
            throw new ApiRequestException("Request interrupted while fetching server info", e);
        } catch (ApiRequestException e) {
            throw e; // Rethrow ApiRequestExceptions without wrapping
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching server info from GameTools API", e);
            throw new ApiRequestException("Error occurred while fetching server info from GameTools API", e);
        }
    }
}