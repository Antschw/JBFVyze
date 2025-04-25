package fr.antschw.bfv.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.antschw.bfv.application.port.UserStatsCachePort;
import fr.antschw.bfv.domain.model.UserStats;
import jakarta.inject.Singleton;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON + in-memory cache adapter.
 */
@Singleton
public class JsonUserStatsCacheAdapter implements UserStatsCachePort {

    private static final Duration TTL = Duration.ofDays(5);
    private static final Path CACHE_FILE = Path.of(System.getProperty("user.home"), ".bfvyze", "statsCache.json");

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, CachedUserStats> cache = new ConcurrentHashMap<>();

    public JsonUserStatsCacheAdapter() {
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
    }

    @Override
    public Optional<UserStats> getCachedStats(String username) {
        var entry = cache.get(username);
        if (entry == null || entry.fetchTime.isBefore(Instant.now().minus(TTL))) {
            cache.remove(username);
            return Optional.empty();
        }
        return Optional.of(entry.stats);
    }

    @Override
    public void putStats(UserStats stats) {
        cache.put(stats.username(), new CachedUserStats(stats, Instant.now()));
    }

    private void loadFromDisk() {
        try {
            var file = CACHE_FILE.toFile();
            if (file.exists()) {
                var disk = mapper.readValue(file, new TypeReference<Map<String, CachedUserStats>>() {});
                cache.putAll(disk);
            }
        } catch (Exception ignored) { }
    }

    private void saveToDisk() {
        try {
            var dir = CACHE_FILE.getParent().toFile();
            if (!dir.exists() && !dir.mkdirs()) throw new RuntimeException("Cannot create cache dir");
            mapper.writerWithDefaultPrettyPrinter().writeValue(CACHE_FILE.toFile(), cache);
        } catch (Exception ignored) { }
    }

    private record CachedUserStats(UserStats stats, Instant fetchTime) {}
}
