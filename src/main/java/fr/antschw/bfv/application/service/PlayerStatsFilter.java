package fr.antschw.bfv.application.service;

import fr.antschw.bfv.domain.model.UserStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters players based on interesting statistical thresholds.
 */
public class PlayerStatsFilter {

    private static final double KD_THRESHOLD = 3.5;
    private static final double KPM_THRESHOLD = 2.5;
    private static final int RANK_THRESHOLD = 500;
    private static final double ACCURACY_THRESHOLD = 27.0;

    /**
     * Determines if a player's stats are considered "interesting".
     *
     * @param stats the user stats to evaluate
     * @return list of matched criteria descriptions, empty if none
     */
    public List<String> getInterestingMetrics(UserStats stats) {
        List<String> matched = new ArrayList<>();

        if (stats.getKillDeath() >= KD_THRESHOLD) {
            matched.add("K/D >= " + KD_THRESHOLD + " → " + stats.getKillDeath());
        }

        if (stats.getKillsPerMinute() >= KPM_THRESHOLD) {
            matched.add("KPM >= " + KPM_THRESHOLD + " → " + stats.getKillsPerMinute());
        }

        if (stats.getRank() == RANK_THRESHOLD) {
            matched.add("Rank = " + RANK_THRESHOLD);
        }

        try {
            double accuracy = Double.parseDouble(stats.getAccuracy().replace("%", ""));
            if (accuracy >= ACCURACY_THRESHOLD) {
                matched.add("Accuracy >= " + ACCURACY_THRESHOLD + "% → " + stats.getAccuracy());
            }
        } catch (NumberFormatException ignored) {
            // Skip if parsing fails
        }

        return matched;
    }
}
