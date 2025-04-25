package fr.antschw.bfv.domain.model;

/**
 * Domain model representing a user's game statistics.
 */
public record UserStats(String username, long userId, long playerId, int rank, double killsPerMinute, String accuracy,
                        String headshots, String timePlayed, long secondsPlayed, int kills, int deaths,
                        double killDeath) {

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
    public UserStats {
    }

    @Override
    public String toString() {
        return String.format("%s [Rank: %d] - K/D: %.2f, KPM: %.2f, Accuracy: %s, HS: %s",
                username, rank, killDeath, killsPerMinute, accuracy, headshots);
    }
}