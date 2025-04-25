package fr.antschw.bfv.infrastructure.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import fr.antschw.bfv.common.constants.ApiConstants;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ServerInfoClient;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for GameTools API to fetch server info using Jackson.
 */
@Singleton
public class GameToolsApiClient implements ServerInfoClient {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Inject
    public GameToolsApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                .build();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ServerInfo fetchServerInfo(String shortServerId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsUri(shortServerId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiRequestException(
                        "GameTools API returned status code: " + response.statusCode()
                );
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode servers = root.path(ApiConstants.JSON_SERVERS);
            if (!servers.isArray() || servers.isEmpty()) {
                throw new ApiRequestException(
                        "No servers found for short ID: " + shortServerId
                );
            }

            for (JsonNode srv : servers) {
                String prefix = srv.path(ApiConstants.JSON_PREFIX).asText("");
                if (prefix.contains("#" + shortServerId)) {
                    long longId = Long.parseLong(srv.path(ApiConstants.JSON_GAMEID).asText());
                    return new ServerInfo(
                            "Server #" + shortServerId,
                            shortServerId,
                            longId,
                            0
                    );
                }
            }

            throw new ApiRequestException(
                    "No matching server found for short ID: " + shortServerId
            );
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Error fetching server info", e);
        }
    }
}
