package fr.antschw.bfv.infrastructure.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import fr.antschw.bfv.application.util.AppConstants;
import fr.antschw.bfv.domain.model.HackersSummary;
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
            HackersSummary summary = parseHackersSummary(json);
            
            long longId = Long.parseLong(serverId);
            ServerInfo info = new ServerInfo(
                    "BFV Server " + serverId,
                    "",
                    longId,
                    summary.numHackers()
            );
            
            // Stocker le résumé complet dans la variable statique pour accès par ServerView
            latestSummary = summary;
            
            return info;
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Error fetching cheater data", e);
        }
    }
    
    /**
     * Parse les données JSON en objet HackersSummary.
     * 
     * @param json Le nœud JSON contenant la réponse de l'API
     * @return Un objet HackersSummary
     */
    private HackersSummary parseHackersSummary(JsonNode json) {
        return new HackersSummary(
                json.path("total_players").asInt(0),
                json.path("num_legit").asInt(0),
                json.path("num_sus").asInt(0),
                json.path("num_v_sus").asInt(0),
                json.path("num_hackers").asInt(0),
                json.path("age").asInt(0)
        );
    }
    
    // Variable statique pour stocker le dernier résumé obtenu
    private static HackersSummary latestSummary;
    
    /**
     * Permet d'obtenir le dernier résumé des hackers récupéré par l'API.
     * 
     * @return Le dernier résumé ou null si aucun appel n'a été effectué
     */
    public static HackersSummary getLatestSummary() {
        return latestSummary;
    }
}