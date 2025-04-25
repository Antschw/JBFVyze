package fr.antschw.bfv.domain.model;

/**
 * Domain model representing a player in a server.
 */
public record ServerPlayer(String name, long playerId, long userId, String platoon, int rank, String team) {

    /**
     * Constructor.
     *
     * @param name     the player's name
     * @param playerId the unique player ID
     * @param userId   the user ID
     * @param platoon  the player's platoon tag (if any)
     * @param rank     the player's rank (0 if not available)
     * @param team     the team name
     */
    public ServerPlayer {
    }

    @Override
    public String toString() {
        return String.format("%s%s [Rank: %d] (%s)",
                name,
                platoon.isEmpty() ? "" : " [" + platoon + "]",
                rank,
                team);
    }
}