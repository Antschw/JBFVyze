package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.model.UserStats;
import java.util.Optional;

/**
 * Port for caching player statistics.
 */
public interface UserStatsCacheService {
    Optional<UserStats> getCachedStats(String username);
    void putStats(UserStats stats);
}
