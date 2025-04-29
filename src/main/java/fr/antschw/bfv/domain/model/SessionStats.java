package fr.antschw.bfv.domain.model;

import java.time.Instant;

/**
 * Domain model representing a player's statistics snapshot during a session.
 */
public record SessionStats(
        String username,
        Instant timestamp,
        int kills,
        int deaths,
        double killDeath,
        double killsPerMinute,
        String accuracy,
        String headshots,
        long secondsPlayed) {

    /**
     * Constructor.
     *
     * @param username       the player's username
     * @param timestamp      when these stats were recorded
     * @param kills          total kills at this point
     * @param deaths         total deaths at this point
     * @param killDeath      kill/death ratio
     * @param killsPerMinute kills per minute ratio
     * @param accuracy       accuracy percentage (as string with %)
     * @param headshots      headshot percentage (as string with %)
     * @param secondsPlayed  total seconds played
     */
    public SessionStats {
    }

    /**
     * Calculate the difference in kills from another snapshot.
     *
     * @param other the previous snapshot
     * @return number of kills gained
     */
    public int killsDifference(SessionStats other) {
        return kills - other.kills;
    }

    /**
     * Calculate the difference in deaths from another snapshot.
     *
     * @param other the previous snapshot
     * @return number of deaths accumulated
     */
    public int deathsDifference(SessionStats other) {
        return deaths - other.deaths;
    }

    /**
     * Calculate time played difference in seconds.
     *
     * @param other the previous snapshot
     * @return seconds played between snapshots
     */
    public long playTimeDifference(SessionStats other) {
        return secondsPlayed - other.secondsPlayed;
    }
}