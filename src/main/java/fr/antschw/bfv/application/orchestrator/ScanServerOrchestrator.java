package fr.antschw.bfv.application.orchestrator;

import fr.antschw.bfv.common.constants.ScanConstants;
import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.ApiRequestException;
import fr.antschw.bfv.domain.service.HotkeyListenerException;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.domain.service.ScreenshotService;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsApiClient;
import fr.antschw.bfvocr.api.BFVOcrFactory;
import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.exceptions.BFVOcrException;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

/**
 * Orchestrates the complete server scanning process using the BFVOcrLibrary.
 */
public class ScanServerOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanServerOrchestrator.class);

    private final HotkeyListenerService hotkeyListenerService;
    private final ScreenshotService screenshotService;
    private final BFVOcrService ocrService;
    private final GameToolsApiClient gameToolsApiClient;
    private final BfvHackersApiClient bfvHackersApiClient;

    /**
     * Constructor with dependency injection.
     *
     * @param hotkeyListenerService service listening for hotkey
     * @param screenshotService     service capturing screenshots
     * @param gameToolsApiClient    Gametools API client for server info
     * @param bfvHackersApiClient   BFV Hackers API client for cheater information
     */
    @Inject
    public ScanServerOrchestrator(HotkeyListenerService hotkeyListenerService,
                                  ScreenshotService screenshotService,
                                  GameToolsApiClient gameToolsApiClient,
                                  BfvHackersApiClient bfvHackersApiClient) {
        this.hotkeyListenerService = hotkeyListenerService;
        this.screenshotService = screenshotService;
        this.ocrService = BFVOcrFactory.createDefaultService();
        this.gameToolsApiClient = gameToolsApiClient;
        this.bfvHackersApiClient = bfvHackersApiClient;
    }

    /**
     * Starts listening and orchestrates scanning.
     *
     * @throws HotkeyListenerException if listener setup fails
     */
    public void start() throws HotkeyListenerException {
        LOGGER.info(ScanConstants.SCAN_START_MESSAGE);
        hotkeyListenerService.startListening(this::scanServer);
    }

    private void scanServer() {
        try {
            LOGGER.info("Capturing screenshot...");
            BufferedImage screenshot = screenshotService.captureScreenshot();

            LOGGER.info("Processing OCR with BFVOcr library...");
            String serverId = ocrService.extractServerNumber(screenshot);
            LOGGER.info("Extracted server ID: {}", serverId);

            ServerInfo gameToolsInfo = gameToolsApiClient.fetchServerInfo(serverId);
            LOGGER.info("Server: {}, Long ID: {}", gameToolsInfo.getServerName(), gameToolsInfo.getLongServerId());

            ServerInfo hackersInfo = bfvHackersApiClient.fetchServerInfo(String.valueOf(gameToolsInfo.getLongServerId()));
            LOGGER.info("Cheaters detected: {}", hackersInfo.getCheaterCount());

            // Combine the two
            ServerInfo completeInfo = new ServerInfo(
                    gameToolsInfo.getServerName(),
                    gameToolsInfo.getShortServerId(),
                    gameToolsInfo.getLongServerId(),
                    hackersInfo.getCheaterCount()
            );

            LOGGER.info("Server {}, Short ID: {}, Long ID: {}, Cheaters: {}",
                    completeInfo.getServerName(),
                    completeInfo.getShortServerId(),
                    completeInfo.getLongServerId(),
                    completeInfo.getCheaterCount()
            );

            LOGGER.info(ScanConstants.SCAN_SUCCESS_MESSAGE);

        } catch (BFVOcrException e) {
            LOGGER.error("OCR processing failed: {}", e.getMessage(), e);
        } catch (ApiRequestException e) {
            LOGGER.error("API request failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred during scan", e);
        }
    }

    /**
     * Stops listening and releases resources.
     */
    public void stop() {
        hotkeyListenerService.stopListening();
        BFVOcrFactory.shutdown();
    }
}