package fr.antschw.bfv.infrastructure.api.util;

import fr.antschw.bfv.common.constants.ApiConstants;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for building API request URIs.
 */
public final class ApiUrlBuilder {

    private ApiUrlBuilder() {
        // Prevent instantiation
    }

    /**
     * Builds the URI for GameTools API server request.
     *
     * @param shortServerId the short server identifier
     * @return URI ready to use
     * @throws URISyntaxException if URI is invalid
     */
    public static URI buildGameToolsUri(String shortServerId) throws URISyntaxException {
        return new URIBuilder(ApiConstants.GAMETOOLS_API_BASE_URL + ApiConstants.GAMETOOLS_SERVER_ENDPOINT)
                .addParameter(ApiConstants.QUERY_PARAM_NAME, shortServerId)
                .addParameter(ApiConstants.QUERY_PARAM_REGION, ApiConstants.REGION_ALL)
                .addParameter(ApiConstants.QUERY_PARAM_PLATFORM, ApiConstants.PLATFORM_PC)
                .addParameter(ApiConstants.QUERY_PARAM_LIMIT, ApiConstants.LIMIT_VALUE)
                .build();
    }

    /**
     * Builds the URI for GameTools API players request.
     *
     * @param serverId the server identifier
     * @return URI ready to use
     * @throws URISyntaxException if URI is invalid
     */
    public static URI buildGameToolsPlayersUri(String serverId) throws URISyntaxException {
        return new URIBuilder(ApiConstants.GAMETOOLS_API_BASE_URL + ApiConstants.GAMETOOLS_PLAYERS_ENDPOINT)
                .addParameter(ApiConstants.QUERY_PARAM_NAME, serverId)
                .build();
    }

    /**
     * Builds the URI for GameTools API player stats request.
     *
     * @param playerName the player name
     * @return URI ready to use
     * @throws URISyntaxException if URI is invalid
     */
    public static URI buildGameToolsStatsUri(String playerName) throws URISyntaxException {
        return new URIBuilder(ApiConstants.GAMETOOLS_API_BASE_URL + ApiConstants.GAMETOOLS_STATS_ENDPOINT)
                .addParameter(ApiConstants.QUERY_PARAM_NAME, playerName)
                .addParameter(ApiConstants.QUERY_PARAM_PLATFORM, ApiConstants.PLATFORM_PC)
                .addParameter(ApiConstants.QUERY_PARAM_FORMAT_VALUES, ApiConstants.FORMAT_VALUES_TRUE)
                .addParameter(ApiConstants.QUERY_PARAM_SKIP_BATTLELOG, ApiConstants.SKIP_BATTLELOG_FALSE)
                .addParameter(ApiConstants.QUERY_PARAM_LANG, ApiConstants.LANG_EN_US)
                .build();
    }

    /**
     * Builds the URI for BFVHackers API server request.
     *
     * @param serverId the server identifier
     * @return URI ready to use
     * @throws URISyntaxException if URI is invalid
     */
    public static URI buildBfvHackersUri(String serverId) throws URISyntaxException {
        return new URIBuilder(ApiConstants.BFVHACKERS_API_BASE_URL)
                .addParameter(ApiConstants.QUERY_PARAM_SERVERID, serverId)
                .build();
    }
}