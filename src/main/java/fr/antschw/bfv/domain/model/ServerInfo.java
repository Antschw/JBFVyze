package fr.antschw.bfv.domain.model;

/**
 * Domain model representing server information.
 */
public class ServerInfo {

    private final String serverName;
    private final String shortServerId;
    private final long longServerId;     // Changed from int to long
    private final int cheaterCount;

    /**
     * Constructor.
     *
     * @param serverName    the server name
     * @param shortServerId the short server ID extracted by OCR
     * @param longServerId  the long server ID retrieved from GameTools
     * @param cheaterCount  number of cheaters detected
     */
    public ServerInfo(String serverName, String shortServerId, long longServerId, int cheaterCount) {
        this.serverName = serverName;
        this.shortServerId = shortServerId;
        this.longServerId = longServerId;
        this.cheaterCount = cheaterCount;
    }

    public String getServerName() {
        return serverName;
    }

    public String getShortServerId() {
        return shortServerId;
    }

    public long getLongServerId() {
        return longServerId;
    }

    public int getCheaterCount() {
        return cheaterCount;
    }
}