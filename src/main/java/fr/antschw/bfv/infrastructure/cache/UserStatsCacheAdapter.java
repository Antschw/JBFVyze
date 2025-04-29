package fr.antschw.bfv.infrastructure.cache;

import fr.antschw.bfv.domain.service.UserStatsCacheService;
import fr.antschw.bfv.domain.model.UserStats;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * JSON + in-memory cache adapter.
 */
@Singleton
public class UserStatsCacheAdapter implements UserStatsCacheService {

    private static final Logger LOGGER = getLogger(UserStatsCacheAdapter.class);
    private static final Duration TTL = Duration.ofDays(5);
    private static final Path CACHE_FILE = Path.of(System.getProperty("user.home"), ".bfvyze", "statsCache.json");

    // Configuration plus robuste de Jackson
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // Pour gérer Instant
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<String, CachedUserStats> cache = new ConcurrentHashMap<>();

    @Inject
    public UserStatsCacheAdapter() {
        LOGGER.info("Initializing stats cache adapter");
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered, saving cache...");
            saveToDisk();
        }));
    }

    @Override
    public Optional<UserStats> getCachedStats(String username) {
        var entry = cache.get(username);
        if (entry == null || entry.fetchTime.isBefore(Instant.now().minus(TTL))) {
            if (entry != null) {
                LOGGER.debug("Cache entry for {} is stale, removing", username);
                cache.remove(username);
            }
            return Optional.empty();
        }
        LOGGER.debug("Cache hit for {}", username);
        return Optional.of(entry.stats);
    }

    @Override
    public void putStats(UserStats stats) {
        if (stats == null || stats.username() == null) {
            LOGGER.warn("Attempted to cache null stats or stats with null username");
            return;
        }
        LOGGER.debug("Caching stats for {}", stats.username());
        cache.put(stats.username(), new CachedUserStats(stats, Instant.now()));
    }

    private void loadFromDisk() {
        try {
            var file = CACHE_FILE.toFile();
            if (file.exists()) {
                LOGGER.info("Loading cache from {}", CACHE_FILE);
                try {
                    var diskCache = mapper.readValue(file,
                            new TypeReference<Map<String, CachedUserStats>>() {});
                    cache.putAll(diskCache);
                    LOGGER.info("Loaded {} entries from cache", cache.size());
                } catch (JsonEOFException e) {
                    // Fichier corrompu, probablement interrompu pendant l'écriture
                    LOGGER.warn("Cache file is corrupted, renaming and starting fresh");
                    // Renommer le fichier corrompu au lieu de le supprimer
                    File backupFile = new File(file.getParentFile(), "statsCache.corrupt");
                    if (backupFile.exists()) {
                        backupFile.delete();
                    }
                    file.renameTo(backupFile);
                }
            } else {
                LOGGER.info("No existing cache file found at {}", CACHE_FILE);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load cache from disk", e);
            // Si quelque chose va mal, assurez-vous que le cache est vide pour éviter des problèmes
            cache.clear();
        }
    }

    public synchronized void saveToDisk() {
        if (cache.isEmpty()) {
            LOGGER.info("Cache is empty, nothing to save");
            return;
        }

        try {
            LOGGER.info("Saving {} cache entries", cache.size());
            var dir = CACHE_FILE.getParent().toFile();
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    LOGGER.error("Failed to create directory: {}", dir.getAbsolutePath());
                    return;
                }
            }

            // Écrire d'abord dans un fichier temporaire puis renommer
            File tempFile = new File(dir, "statsCache.tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, cache);

            // Supprimer l'ancien fichier s'il existe
            File targetFile = CACHE_FILE.toFile();
            if (targetFile.exists() && !targetFile.delete()) {
                LOGGER.warn("Could not delete existing cache file");
            }

            // Renommer le fichier temporaire
            if (tempFile.renameTo(targetFile)) {
                LOGGER.info("Cache successfully saved to {}", CACHE_FILE);
            } else {
                LOGGER.error("Failed to rename temporary cache file");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save cache to disk", e);
        }
    }

    record CachedUserStats(UserStats stats, Instant fetchTime) {}
}
