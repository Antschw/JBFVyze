package fr.antschw.bfv.application.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.antschw.bfv.application.usecase.PlayerStatsUseCase;
import fr.antschw.bfv.domain.model.InterestingPlayer;
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
import java.util.function.Consumer;

import static fr.antschw.bfv.common.constants.ApiConstants.BFVHACKERS_NAME;
import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_NAME;

/**
 * Provides each stage of the scan pipeline as isolated methods.
 */
public class ServerScanService {

    private final ScreenshotService screenshotService;
    private final BFVOcrService ocrService;
    private final ApiClient gameToolsApiClient;
    private final ApiClient bfvHackersApiClient;
    private final PlayerStatsUseCase playerStatsUseCase;
    private final PlayerStatsFilter playerStatsFilter;

    /**
     * Constructs the server scan service with all required dependencies.
     */
    @Inject
    public ServerScanService(
            ScreenshotService screenshotService,
            BFVOcrService ocrService,
            @Named(GAMETOOLS_NAME) ApiClient gameToolsApiClient,
            @Named(BFVHACKERS_NAME) ApiClient bfvHackersApiClient,
            PlayerStatsUseCase playerStatsUseCase,
            PlayerStatsFilter playerStatsFilter
    ) {
        this.screenshotService = screenshotService;
        this.ocrService = ocrService;
        this.gameToolsApiClient = gameToolsApiClient;
        this.bfvHackersApiClient = bfvHackersApiClient;
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
        return gameToolsApiClient.fetchServerInfo(shortId);
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
        ServerInfo hackersInfo = bfvHackersApiClient.fetchServerInfo(longId);
        return new ServerInfo(
                baseInfo.serverName(),
                baseInfo.shortServerId(),
                baseInfo.longServerId(),
                hackersInfo.cheaterCount()
        );
    }

    /**
     * Queries all players from the server and filters those with suspicious stats.
     *
     * @param shortId OCR-detected short ID
     */
    public void queryPlayers(String shortId, Consumer<InterestingPlayer> callback) {
        List<InterestingPlayer> flagged = new ArrayList<>();
        try {
            ServerPlayers players = playerStatsUseCase.getServerPlayers(shortId);
            for (ServerPlayer player : players.players()) {
                try {
                    UserStats stats = playerStatsUseCase.getPlayerStats(player.name());
                    List<String> metrics = playerStatsFilter.getInterestingMetrics(stats);
                    if (!metrics.isEmpty()) {
                        InterestingPlayer flaggedPlayer = new InterestingPlayer(player.name(), metrics);
                        flagged.add(flaggedPlayer);
                        callback.accept(flaggedPlayer);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

}
