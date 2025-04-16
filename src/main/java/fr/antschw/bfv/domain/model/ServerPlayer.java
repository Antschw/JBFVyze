package fr.antschw.bfv.domain.model;

/**
 * Domain model representing a player in a server.
 */
public class ServerPlayer {

    private final String name;
    private final long playerId;
    private final long userId;
    private final String platoon;
    private final int rank;
    private final String team;

    /**
     * Constructor.
     *
     * @param name      the player's name
     * @param playerId  the unique player ID
     * @param userId    the user ID
     * @param platoon   the player's platoon tag (if any)
     * @param rank      the player's rank (0 if not available)
     * @param team      the team name
     */
    public ServerPlayer(String name, long playerId, long userId, String platoon, int rank, String team) {
        this.name = name;
        this.playerId = playerId;
        this.userId = userId;
        this.platoon = platoon;
        this.rank = rank;
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public long getPlayerId() {
        return playerId;
    }

    public long getUserId() {
        return userId;
    }

    public String getPlatoon() {
        return platoon;
    }

    public int getRank() {
        return rank;
    }

    public String getTeam() {
        return team;
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