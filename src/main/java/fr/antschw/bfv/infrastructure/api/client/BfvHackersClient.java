package fr.antschw.bfv.infrastructure.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import fr.antschw.bfv.application.util.constants.AppConstants;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ServerInfoService;
import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for BFVHackers API to fetch cheater count using Jackson.
 */
@Singleton
public class BfvHackersClient implements ServerInfoService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Inject
    public BfvHackersClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConstants.HTTP_TIMEOUT_SECONDS))
                .build();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ServerInfo fetchServerInfo(String serverId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildBfvHackersUri(serverId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(AppConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiRequestException(
                        "BFVHackers API returned status code: " + response.statusCode()
                );
            }

            JsonNode json = mapper.readTree(response.body());
            int cheaterCount = json.path(AppConstants.JSON_NUM_HACKERS).asInt(0);
            long longId = Long.parseLong(serverId);

            return new ServerInfo(
                    "BFV Server " + serverId,
                    "",
                    longId,
                    cheaterCount
            );
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Error fetching cheater data", e);
        }
    }
}
