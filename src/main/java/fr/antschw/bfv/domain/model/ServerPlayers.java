package fr.antschw.bfv.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Domain model representing all players in a server.
 */
public record ServerPlayers(String serverName, String serverId, List<ServerPlayer> players) {

    /**
     * Constructor.
     *
     * @param serverName the server name
     * @param serverId   the server ID
     * @param players    list of players in the server
     */
    public ServerPlayers {
    }

    @Override
    public List<ServerPlayer> players() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Gets the total number of players in the server.
     *
     * @return total player count
     */
    public int getTotalPlayerCount() {
        return players.size();
    }

    @Override
    public String toString() {
        return String.format("Server %s with %d players", serverName, getTotalPlayerCount());
    }
}