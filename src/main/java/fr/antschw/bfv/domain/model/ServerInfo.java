package fr.antschw.bfv.domain.model;

/**
 * Domain model representing server information.
 *
 * @param longServerId Changed from int to long
 */
public record ServerInfo(String serverName, String shortServerId, long longServerId, int cheaterCount) {

    /**
     * Constructor.
     *
     * @param serverName    the server name
     * @param shortServerId the short server ID extracted by OCR
     * @param longServerId  the long server ID retrieved from GameTools
     * @param cheaterCount  number of cheaters detected
     */
    public ServerInfo {
    }
}