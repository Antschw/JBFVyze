package fr.antschw.bfv.infrastructure.api.client;

import fr.antschw.bfv.common.constants.ApiConstants;
import fr.antschw.bfv.domain.model.ServerPlayer;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.infrastructure.api.util.ApiUrlBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for GameTools API to fetch player-related information.
 */
public class GameToolsPlayerApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameToolsPlayerApiClient.class);
    private final HttpClient httpClient;

    public GameToolsPlayerApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Fetches player list for a given server.
     *
     * @param serverId short server ID
     * @return ServerPlayers object with all players on the server
     * @throws ApiRequestException if the API request fails
     */
    public ServerPlayers fetchServerPlayers(String serverId) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsPlayersUri(serverId);
            LOGGER.debug("Requesting GameTools Players API with URI: {}", uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("GameTools Players API returned status code: {}", response.statusCode());
                throw new ApiRequestException("GameTools Players API returned non-200 status code: " + response.statusCode());
            }

            LOGGER.debug("GameTools Players API response received");
            JSONObject json = new JSONObject(response.body());

            // Check if serverinfo exists
            JSONObject serverInfo = json.optJSONObject("serverinfo");
            if (serverInfo == null) {
                throw new ApiRequestException("Invalid response format: missing serverinfo");
            }

            String serverName = serverInfo.optString("name", "Unknown Server");

            List<ServerPlayer> players = new ArrayList<>();

            // Process teams
            JSONArray teams = json.optJSONArray(ApiConstants.JSON_TEAMS);
            if (teams != null) {
                for (int i = 0; i < teams.length(); i++) {
                    JSONObject team = teams.getJSONObject(i);
                    String teamName = team.optString("name", "Unknown Team");

                    JSONArray teamPlayers = team.optJSONArray(ApiConstants.JSON_PLAYERS);
                    if (teamPlayers != null) {
                        for (int j = 0; j < teamPlayers.length(); j++) {
                            JSONObject playerObj = teamPlayers.getJSONObject(j);

                            String name = playerObj.optString(ApiConstants.JSON_NAME, "Unknown");
                            long playerId = playerObj.optLong(ApiConstants.JSON_PLAYER_ID, 0);
                            long userId = playerObj.optLong(ApiConstants.JSON_USER_ID, 0);
                            String platoon = playerObj.optString(ApiConstants.JSON_PLATOON, "");
                            int rank = playerObj.optInt(ApiConstants.JSON_RANK, 0);

                            players.add(new ServerPlayer(name, playerId, userId, platoon, rank, teamName));
                        }
                    }
                }
            }

            LOGGER.info("Found {} players in server {}", players.size(), serverName);
            return new ServerPlayers(serverName, serverId, players);

        } catch (ApiRequestException e) {
            throw e; // Rethrow ApiRequestExceptions without wrapping
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching server players from GameTools API", e);
            throw new ApiRequestException("Error occurred while fetching server players from GameTools API", e);
        }
    }

    /**
     * Fetches user statistics for a given player name.
     *
     * @param playerName the player name
     * @return UserStats object containing the player's stats
     * @throws ApiRequestException if the API request fails
     */
    public UserStats fetchUserStats(String playerName) throws ApiRequestException {
        try {
            URI uri = ApiUrlBuilder.buildGameToolsStatsUri(playerName);
            LOGGER.debug("Requesting GameTools Stats API with URI: {}", uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(ApiConstants.HTTP_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("GameTools Stats API returned status code: {}", response.statusCode());
                throw new ApiRequestException("GameTools Stats API returned non-200 status code: " + response.statusCode());
            }

            LOGGER.debug("GameTools Stats API response received");
            JSONObject json = new JSONObject(response.body());

            String username = json.optString(ApiConstants.JSON_USERNAME, playerName);
            long userId = json.optLong("userId", 0);
            long playerId = json.optLong("id", 0);
            int rank = json.optInt(ApiConstants.JSON_RANK, 0);
            double killsPerMinute = json.optDouble(ApiConstants.JSON_KILLS_PER_MINUTE, 0.0);
            String accuracy = json.optString(ApiConstants.JSON_ACCURACY, "0%");
            String headshots = json.optString(ApiConstants.JSON_HEADSHOTS, "0%");
            String timePlayed = json.optString(ApiConstants.JSON_TIME_PLAYED, "Unknown");
            long secondsPlayed = json.optLong(ApiConstants.JSON_SECONDS_PLAYED, 0);
            int kills = json.optInt(ApiConstants.JSON_KILLS, 0);
            int deaths = json.optInt(ApiConstants.JSON_DEATHS, 0);
            double killDeath = json.optDouble(ApiConstants.JSON_KILL_DEATH, 0.0);

            UserStats stats = new UserStats(
                    username, userId, playerId, rank,
                    killsPerMinute, accuracy, headshots,
                    timePlayed, secondsPlayed,
                    kills, deaths, killDeath
            );

            LOGGER.info("Retrieved stats for player: {}", username);
            return stats;

        } catch (ApiRequestException e) {
            throw e; // Rethrow ApiRequestExceptions without wrapping
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching user stats from GameTools API", e);
            throw new ApiRequestException("Error occurred while fetching user stats from GameTools API", e);
        }
    }

    /**
     * Fetches user statistics based on player ID.
     *
     * @param playerId the player's unique ID
     * @return UserStats object containing the player's stats
     * @throws ApiRequestException if the API request fails
     */
    public UserStats fetchUserStatsById(long playerId) throws ApiRequestException {
        // For this implementation, we'll convert the ID to a string and use the same method
        // In a real-world scenario, you might want to use a different endpoint or parameter
        return fetchUserStats(String.valueOf(playerId));
    }
}