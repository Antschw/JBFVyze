package fr.antschw.bfv.application.service;

import fr.antschw.bfv.domain.model.InterestingPlayer;
import fr.antschw.bfv.domain.model.ServerInfo;

import java.util.List;

/**
 * Encapsulates the result of a full server scan, including server info and flagged players.
 */
public class ServerScanResult {

    private final ServerInfo serverInfo;
    private final List<InterestingPlayer> interestingPlayers;

    /**
     * Constructs a ServerScanResult.
     *
     * @param serverInfo          combined server info
     * @param interestingPlayers  players with suspicious stats
     */
    public ServerScanResult(ServerInfo serverInfo, List<InterestingPlayer> interestingPlayers) {
        this.serverInfo = serverInfo;
        this.interestingPlayers = interestingPlayers;
    }

    /**
     * Gets the server information.
     *
     * @return server info object
     */
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * Gets the list of interesting players.
     *
     * @return list of flagged players
     */
    public List<InterestingPlayer> getInterestingPlayers() {
        return interestingPlayers;
    }
}
