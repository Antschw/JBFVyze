package fr.antschw.bfv.domain.model;

/**
 * Domain model representing a user's game statistics.
 */
public class UserStats {

    private final String username;
    private final long userId;
    private final long playerId;
    private final int rank;
    private final double killsPerMinute;
    private final String accuracy;
    private final String headshots;
    private final String timePlayed;
    private final long secondsPlayed;
    private final int kills;
    private final int deaths;
    private final double killDeath;

    /**
     * Constructor.
     *
     * @param username       the player's username
     * @param userId         the user ID
     * @param playerId       the player ID
     * @param rank           the player's rank
     * @param killsPerMinute kills per minute ratio
     * @param accuracy       accuracy percentage (as string with %)
     * @param headshots      headshot percentage (as string with %)
     * @param timePlayed     time played (formatted as string)
     * @param secondsPlayed  total seconds played
     * @param kills          total kills
     * @param deaths         total deaths
     * @param killDeath      kill/death ratio
     */
    public UserStats(String username, long userId, long playerId, int rank, double killsPerMinute,
                     String accuracy, String headshots, String timePlayed, long secondsPlayed,
                     int kills, int deaths, double killDeath) {
        this.username = username;
        this.userId = userId;
        this.playerId = playerId;
        this.rank = rank;
        this.killsPerMinute = killsPerMinute;
        this.accuracy = accuracy;
        this.headshots = headshots;
        this.timePlayed = timePlayed;
        this.secondsPlayed = secondsPlayed;
        this.kills = kills;
        this.deaths = deaths;
        this.killDeath = killDeath;
    }

    public String getUsername() {
        return username;
    }

    public long getUserId() {
        return userId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public int getRank() {
        return rank;
    }

    public double getKillsPerMinute() {
        return killsPerMinute;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getHeadshots() {
        return headshots;
    }

    public String getTimePlayed() {
        return timePlayed;
    }

    public long getSecondsPlayed() {
        return secondsPlayed;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public double getKillDeath() {
        return killDeath;
    }

    @Override
    public String toString() {
        return String.format("%s [Rank: %d] - K/D: %.2f, KPM: %.2f, Accuracy: %s, HS: %s",
                username, rank, killDeath, killsPerMinute, accuracy, headshots);
    }
}