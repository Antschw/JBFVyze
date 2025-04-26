package fr.antschw.bfv.application.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.application.Platform;

import fr.antschw.bfv.application.usecase.PlayerStatsUseCase;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.model.ServerPlayer;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.ScreenshotService;
import fr.antschw.bfv.domain.service.ServerInfoClient;
import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.exceptions.BFVOcrException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static fr.antschw.bfv.common.constants.ApiConstants.BFVHACKERS_NAME;
import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_NAME;

/**
 * Service responsible for performing server scans, including
 * screenshot capture, OCR extraction, API queries, and asynchronous
 * retrieval of player statistics.
 */
public class ServerScanService {

    private static final int DEFAULT_THREAD_COUNT = 5;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ServerScanService.class);

    private final ScreenshotService screenshotService;
    private final BFVOcrService ocrService;
    private final ServerInfoClient gameToolsInfoClient;
    private final ServerInfoClient bfvHackersInfoClient;
    private final PlayerStatsUseCase playerStatsUseCase;
    private final PlayerStatsFilter playerStatsFilter;
    private final ExecutorService executor;

    /**
     * Constructs the server scan service with all required dependencies
     * and initializes the thread pool.
     */
    @Inject
    public ServerScanService(
            ScreenshotService screenshotService,
            BFVOcrService ocrService,
            @Named(GAMETOOLS_NAME) ServerInfoClient gameToolsInfoClient,
            @Named(BFVHACKERS_NAME) ServerInfoClient bfvHackersInfoClient,
            PlayerStatsUseCase playerStatsUseCase,
            PlayerStatsFilter playerStatsFilter
    ) {
        this.screenshotService = screenshotService;
        this.ocrService = ocrService;
        this.gameToolsInfoClient = gameToolsInfoClient;
        this.bfvHackersInfoClient = bfvHackersInfoClient;
        this.playerStatsUseCase = playerStatsUseCase;
        this.playerStatsFilter = playerStatsFilter;
        this.executor = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);
    }

    /**
     * Performs screenshot capture and OCR to extract the server's short ID.
     *
     * @return the detected server short ID
     * @throws Exception if capture or OCR fails
     */
    public String extractServerId() throws Exception {
        BufferedImage image = screenshotService.captureScreenshot();
        Optional<String> result = ocrService.tryExtractServerNumber(image);

        if (result.isEmpty()) {
            throw new BFVOcrException("No server number detected.");
        }
        return result.get();
    }

    /**
     * Queries the GameTools API to retrieve full server information using
     * the OCR-detected short ID.
     *
     * @param shortId the OCR-detected server ID
     * @return the server info containing full details
     * @throws Exception if the API request fails
     */
    public ServerInfo queryGameTools(String shortId) throws Exception {
        return gameToolsInfoClient.fetchServerInfo(shortId);
    }

    /**
     * Queries the BFVHackers API and merges results with base server info
     * to include cheater count.
     *
     * @param longId   the full server ID from GameTools
     * @param baseInfo the base server info from GameTools
     * @return enriched server info including cheater count
     * @throws Exception if the API request fails
     */
    public ServerInfo queryBfvHackers(String longId, ServerInfo baseInfo) throws Exception {
        ServerInfo hackersInfo = bfvHackersInfoClient.fetchServerInfo(longId);
        return new ServerInfo(
                baseInfo.serverName(),
                baseInfo.shortServerId(),
                baseInfo.longServerId(),
                hackersInfo.cheaterCount()
        );
    }

    /**
     * Retrieves all players on the server and processes their statistics asynchronously.
     * Updates the UI via the provided callbacks on the JavaFX Application thread.
     *
     * @param shortId        the OCR-detected server ID
     * @param playerCallback callback invoked for each loaded player
     * @param statsCallback  callback invoked for each player's stats
     */
    public void queryPlayersAsync(
            String shortId,
            Consumer<ServerPlayer> playerCallback,
            BiConsumer<ServerPlayer, UserStats> statsCallback
    ) {
        try {
            ServerPlayers players = playerStatsUseCase.getServerPlayers(shortId);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (ServerPlayer player : players.players()) {
                Platform.runLater(() -> playerCallback.accept(player));

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        UserStats stats = playerStatsUseCase.getPlayerStats(player.name());
                        playerStatsFilter.getInterestingMetrics(stats);
                        Platform.runLater(() -> statsCallback.accept(player, stats));
                    } catch (Exception e) {
                        LOGGER.warn("Failed to fetch stats for player {}: {}", player.name(), e.getMessage());
                        Platform.runLater(() -> statsCallback.accept(player, null));
                    }
                }, executor);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            LOGGER.error("Error fetching players list: {}", e.getMessage(), e);
        }
    }

    /**
     * Gracefully shuts down the internal thread pool used for player queries.
     * Ensures any running tasks are terminated.
     */
    public void shutdown() {
        LOGGER.info("Shutting down scan executor...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Executor did not terminate in time, forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}