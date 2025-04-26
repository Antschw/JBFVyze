package fr.antschw.bfv.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for browser-related operations.
 */
public final class BrowserUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserUtils.class);
    
    /**
     * Base URL for BFVHackers player profiles.
     */
    public static final String BFVHACKERS_PLAYER_URL = "https://bfvhackers.com/?player-id=";

    private BrowserUtils() {
        // Prevent instantiation
    }

    /**
     * Opens the default browser with a URL to the BFVHackers profile for the given player ID.
     *
     * @param playerId the player's ID
     * @return true if successful, false otherwise
     */
    public static boolean openPlayerProfile(long playerId) {
        if (playerId <= 0) {
            LOGGER.warn("Invalid player ID: {}", playerId);
            return false;
        }
        
        return openUrl(BFVHACKERS_PLAYER_URL + playerId);
    }

    /**
     * Opens the default browser with the given URL.
     *
     * @param url the URL to open
     * @return true if successful, false otherwise
     */
    public static boolean openUrl(String url) {
        if (url == null || url.isBlank()) {
            LOGGER.warn("Invalid URL: {}", url);
            return false;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            } else {
                LOGGER.warn("Desktop browsing not supported on this platform");
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed to open URL: {}", url, e);
            return false;
        }
    }
}