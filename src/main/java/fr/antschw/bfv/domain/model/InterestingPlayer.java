package fr.antschw.bfv.domain.model;

import java.util.List;

/**
 * Represents a player flagged with interesting statistics.
 */
public class InterestingPlayer {

    private final String name;
    private final List<String> matchingMetrics;

    /**
     * Constructs an interesting player.
     *
     * @param name             the player name
     * @param matchingMetrics  list of metrics that triggered the flag
     */
    public InterestingPlayer(String name, List<String> matchingMetrics) {
        this.name = name;
        this.matchingMetrics = matchingMetrics;
    }

    /**
     * Returns the player name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of matched interesting metrics.
     *
     * @return list of metrics
     */
    public List<String> getMatchingMetrics() {
        return matchingMetrics;
    }

    @Override
    public String toString() {
        return name + " → " + String.join(", ", matchingMetrics);
    }
}
