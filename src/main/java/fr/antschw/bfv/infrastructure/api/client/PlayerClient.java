package fr.antschw.bfv.infrastructure.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import fr.antschw.bfv.application.util.constants.AppConstants;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.ServerPlayer;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.ServerPlayersService;
import fr.antschw.bfv.domain.service.UserStatsService;
import fr.antschw.bfv.domain.exception.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for GameTools API to fetch player list and individual stats using Jackson.
 */
@Singleton
public class PlayerClient
        implements ServerPlayersService, UserStatsService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Inject
    public PlayerClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConstants.HTTP_TIMEOUT_SECONDS))
                .build();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ServerPlayers fetchServerPlayers(String serverId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsPlayersUri(serverId);
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
                        "GameTools Players API returned status code: " + response.statusCode()
                );
            }

            JsonNode root = mapper.readTree(response.body());
            String serverName = root.path("serverinfo")
                    .path("name")
                    .asText("Unknown Server");

            List<ServerPlayer> players = new ArrayList<>();
            JsonNode teams = root.path(AppConstants.JSON_TEAMS);
            if (teams.isArray()) {
                for (JsonNode team : teams) {
                    String teamName = team.path("name").asText("Unknown Team");
                    for (JsonNode p : team.path(AppConstants.JSON_PLAYERS)) {
                        players.add(new ServerPlayer(
                                p.path(AppConstants.JSON_NAME).asText("Unknown"),
                                p.path(AppConstants.JSON_PLAYER_ID).asLong(0),
                                p.path(AppConstants.JSON_USER_ID).asLong(0),
                                p.path(AppConstants.JSON_PLATOON).asText(""),
                                p.path(AppConstants.JSON_RANK).asInt(0),
                                teamName
                        ));
                    }
                }
            }

            return new ServerPlayers(serverName, serverId, players);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Error fetching server players", e);
        }
    }

    @Override
    public UserStats fetchUserStats(String playerName) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsStatsUri(playerName);
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
                        "GameTools Stats API returned status code: " + response.statusCode()
                );
            }

            JsonNode json = mapper.readTree(response.body());
            return new UserStats(
                    json.path(AppConstants.JSON_USERNAME).asText(playerName),
                    json.path("userId").asLong(0),
                    json.path("id").asLong(0),
                    json.path(AppConstants.JSON_RANK).asInt(0),
                    json.path(AppConstants.JSON_KILLS_PER_MINUTE).asDouble(0.0),
                    json.path(AppConstants.JSON_ACCURACY).asText("0%"),
                    json.path(AppConstants.JSON_HEADSHOTS).asText("0%"),
                    json.path(AppConstants.JSON_TIME_PLAYED).asText("Unknown"),
                    json.path(AppConstants.JSON_SECONDS_PLAYED).asLong(0),
                    json.path(AppConstants.JSON_KILLS).asInt(0),
                    json.path(AppConstants.JSON_DEATHS).asInt(0),
                    json.path(AppConstants.JSON_KILL_DEATH).asDouble(0.0)
            );
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Error fetching user stats", e);
        }
    }
}
