package fr.antschw.bfv.infrastructure.api.util;

import fr.antschw.bfv.application.util.constants.AppConstants;
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
        return new URIBuilder(AppConstants.GAMETOOLS_API_BASE_URL + AppConstants.GAMETOOLS_SERVER_ENDPOINT)
                .addParameter(AppConstants.QUERY_PARAM_NAME, shortServerId)
                .addParameter(AppConstants.QUERY_PARAM_REGION, AppConstants.REGION_ALL)
                .addParameter(AppConstants.QUERY_PARAM_PLATFORM, AppConstants.PLATFORM_PC)
                .addParameter(AppConstants.QUERY_PARAM_LIMIT, AppConstants.LIMIT_VALUE)
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
        return new URIBuilder(AppConstants.GAMETOOLS_API_BASE_URL + AppConstants.GAMETOOLS_PLAYERS_ENDPOINT)
                .addParameter(AppConstants.QUERY_PARAM_NAME, serverId)
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
        return new URIBuilder(AppConstants.GAMETOOLS_API_BASE_URL + AppConstants.GAMETOOLS_STATS_ENDPOINT)
                .addParameter(AppConstants.QUERY_PARAM_NAME, playerName)
                .addParameter(AppConstants.QUERY_PARAM_PLATFORM, AppConstants.PLATFORM_PC)
                .addParameter(AppConstants.QUERY_PARAM_FORMAT_VALUES, AppConstants.FORMAT_VALUES_TRUE)
                .addParameter(AppConstants.QUERY_PARAM_SKIP_BATTLELOG, AppConstants.SKIP_BATTLELOG_FALSE)
                .addParameter(AppConstants.QUERY_PARAM_LANG, AppConstants.LANG_EN_US)
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
        return new URIBuilder(AppConstants.BFVHACKERS_API_BASE_URL)
                .addParameter(AppConstants.QUERY_PARAM_SERVERID, serverId)
                .build();
    }
}