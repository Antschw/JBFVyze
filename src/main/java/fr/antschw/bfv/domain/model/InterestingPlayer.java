package fr.antschw.bfv.domain.model;

import java.util.List;

/**
 * Represents a player flagged with interesting statistics.
 */
public record InterestingPlayer(String name, List<String> matchingMetrics) {

    /**
     * Constructs an interesting player.
     *
     * @param name            the player name
     * @param matchingMetrics list of metrics that triggered the flag
     */
    public InterestingPlayer {
    }

    /**
     * Returns the player name.
     *
     * @return name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Returns the list of matched interesting metrics.
     *
     * @return list of metrics
     */
    @Override
    public List<String> matchingMetrics() {
        return matchingMetrics;
    }

    @Override
    public String toString() {
        return name + " → " + String.join(", ", matchingMetrics);
    }
}
