package fr.antschw.bfv.application.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.application.Platform;

import fr.antschw.bfv.application.usecase.PlayerStatsUseCase;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.model.ServerPlayer;
import fr.antschw.bfv.domain.model.ServerPlayers;
import fr.antschw.bfv.domain.model.UserStats;
import fr.antschw.bfv.domain.service.*;
import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.exceptions.BFVOcrException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static fr.antschw.bfv.common.constants.ApiConstants.BFVHACKERS_NAME;
import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_NAME;

/**
 * Provides each stage of the scan pipeline as isolated methods.
 */
public class ServerScanService {

    private final ScreenshotService screenshotService;
    private final BFVOcrService ocrService;
    private final ServerInfoClient gameToolsInfoClient;
    private final ServerInfoClient bfvHackersInfoClient;
    private final PlayerStatsUseCase playerStatsUseCase;
    private final PlayerStatsFilter playerStatsFilter;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ServerScanService.class);

    /**
     * Constructs the server scan service with all required dependencies.
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
        this.gameToolsInfoClient   = gameToolsInfoClient;
        this.bfvHackersInfoClient  = bfvHackersInfoClient;
        this.playerStatsUseCase = playerStatsUseCase;
        this.playerStatsFilter = playerStatsFilter;
    }

    /**
     * Performs screenshot + OCR to extract the short server ID.
     *
     * @return server short ID
     * @throws Exception if OCR or capture fails
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
     * Queries GameTools to retrieve server info.
     *
     * @param shortId OCR-detected short ID
     * @return server info with long ID
     * @throws Exception if query fails
     */
    public ServerInfo queryGameTools(String shortId) throws Exception {
        return gameToolsInfoClient.fetchServerInfo(shortId);
    }

    /**
     * Queries BFVHackers and combines info into a complete ServerInfo object.
     *
     * @param longId     long ID from GameTools
     * @param baseInfo   base server info from GameTools
     * @return updated ServerInfo with cheater count
     * @throws Exception if query fails
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
     * Queries all players from the server and processes their stats asynchronously.
     *
     * @param shortId OCR-detected short ID
     * @param playerCallback callback for each player as they’re loaded
     * @param statsCallback callback for when a player's stats are loaded
     */
    public void queryPlayersAsync(String shortId,
                                  Consumer<ServerPlayer> playerCallback,
                                  BiConsumer<ServerPlayer, UserStats> statsCallback) {
        ExecutorService executor = Executors.newFixedThreadPool(5); // 5 threads pour les requêtes parallèles

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
            LOGGER.error("Error fetching players list: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

}
