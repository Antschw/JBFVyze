package fr.antschw.bfv.application.port;

import fr.antschw.bfv.domain.model.UserStats;
import java.util.Optional;

/**
 * Port for caching player statistics.
 */
public interface UserStatsCachePort {
    Optional<UserStats> getCachedStats(String username);
    void putStats(UserStats stats);
}
