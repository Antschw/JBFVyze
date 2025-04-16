package fr.antschw.bfv.infrastructure.api.client;

import fr.antschw.bfv.common.constants.ApiConstants;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;
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
 * Implementation of ApiClient for BFVHackers API.
 */
public class BfvHackersApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BfvHackersApiClient.class);
    private final HttpClient httpClient;

    public BfvHackersApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                .build();
    }

    @Override
    public ServerInfo fetchServerInfo(String serverId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildBfvHackersUri(serverId);
            LOGGER.debug("Requesting BFVHackers API with URI: {}", uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("BFVHackers API returned status code: {}", response.statusCode());
                throw new ApiRequestException("BFVHackers API returned non-200 status code: " + response.statusCode());
            }

            LOGGER.debug("BFVHackers API response: {}", response.body());
            JSONObject json = new JSONObject(response.body());
            int cheaterCount = json.optInt(ApiConstants.JSON_NUM_HACKERS, 0);
            LOGGER.info("Found {} cheaters for server ID: {}", cheaterCount, serverId);

            try {
                long longId = Long.parseLong(serverId);
                return new ServerInfo(
                        "BFV Server " + serverId,
                        "",
                        longId,
                        cheaterCount
                );
            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse serverId as long: {}", serverId, e);
                throw new ApiRequestException("Invalid serverId format: " + serverId, e);
            }

        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI for BFVHackers API", e);
            throw new ApiRequestException("Invalid URI for BFVHackers API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Request interrupted while fetching server info", e);
            throw new ApiRequestException("Request interrupted while fetching server info", e);
        } catch (ApiRequestException e) {
            throw e; // Rethrow ApiRequestExceptions without wrapping
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching server info from BFVHackers API", e);
            throw new ApiRequestException("Error occurred while fetching server info from BFVHackers API", e);
        }
    }
}