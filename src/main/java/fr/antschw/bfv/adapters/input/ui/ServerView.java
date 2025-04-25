package fr.antschw.bfv.adapters.input.ui;

import fr.antschw.bfv.adapters.input.window.PlayersPanel;
import fr.antschw.bfv.adapters.input.window.ScanControlPane;
import fr.antschw.bfv.adapters.input.window.StatusPanel;
import fr.antschw.bfv.application.service.PlayerStatsFilter;
import fr.antschw.bfv.application.service.ServerScanService;
import fr.antschw.bfv.common.constants.UIConstants;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerException;
import fr.antschw.bfv.domain.service.HotkeyListenerService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.time.Duration;
import java.time.Instant;

/**
 * Orchestrates the scan flow by delegating to three panels:
 * ScanControlPane, StatusPanel, and PlayersPanel.
 */
public class ServerView {

    private final VBox root = new VBox(15);
    private final ServerScanService scanService;
    private final HotkeyConfigurationService hotkeyConfig;
    private final PlayerStatsFilter playerStatsFilter;
    private final ScanControlPane controlPane;
    private final StatusPanel statusPane = new StatusPanel();
    private final PlayersPanel playersPane = new PlayersPanel();

    @Inject
    public ServerView(ServerScanService scanService,
                      HotkeyConfigurationService hotkeyConfig,
                      HotkeyListenerService hotkeyListener,
                      PlayerStatsFilter playerStatsFilter) {
        this.scanService = scanService;
        this.hotkeyConfig = hotkeyConfig;
        this.playerStatsFilter = playerStatsFilter;

        controlPane = new ScanControlPane(hotkeyConfig, this::runScan);
        // Add 20px padding around all sides
        root.setPadding(new Insets(UIConstants.WINDOW_PADDING));
        root.getChildren().addAll(controlPane, statusPane, new Separator(), playersPane);

        try {
            hotkeyListener.startListening(() -> Platform.runLater(this::runScan));
        } catch (HotkeyListenerException e) {
            // log error...
        }
    }

    private void runScan() {
        controlPane.setScanning(true);
        statusPane.reset();
        playersPane.startLoading();

        Instant start = Instant.now();

        new Thread(() -> {
            try {
                // 1) OCR
                String shortId = scanService.extractServerId();
                Platform.runLater(() ->
                        statusPane.setOcrStatus("OCR: #" + shortId, false)
                );

                // 2) GameTools
                var info = scanService.queryGameTools(shortId);
                Platform.runLater(() ->
                        statusPane.setGameToolsStatus("GameTools: ID " + info.longServerId(), false)
                );

                // 3) Hackers
                var hackInfo = scanService.queryBfvHackers(String.valueOf(info.longServerId()), info);
                boolean hasCheaters = hackInfo.cheaterCount() > 0;
                Platform.runLater(() ->
                        statusPane.setHackersStatus(
                                "BFVHackers: " + hackInfo.cheaterCount() + " cheaters",
                                false, hasCheaters
                        )
                );

                // 4) Players async
                scanService.queryPlayersAsync(
                        shortId,
                        player -> Platform.runLater(() -> playersPane.addPlayer(player.name())),
                        (player, stats) -> Platform.runLater(() -> {
                            boolean suspicious = stats != null &&
                                    !playerStatsFilter.getInterestingMetrics(stats).isEmpty();

                            playersPane.updatePlayer(
                                    player.name(),
                                    stats != null ? stats.killDeath() : null,
                                    stats != null ? stats.killsPerMinute() : null,
                                    stats != null ? stats.rank() : null,
                                    stats != null ? stats.accuracy() : null,
                                    suspicious
                            );
                        })
                );

                Duration d = Duration.between(start, Instant.now());
                String time = String.format("Time: %dm %ds", d.toMinutes(), d.minusMinutes(d.toMinutes()).getSeconds());
                Platform.runLater(() -> statusPane.setTime(time));

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusPane.setOcrStatus("Error: " + ex.getMessage(), false);
                    statusPane.setGameToolsStatus("", false);
                    statusPane.setHackersStatus("", false, false);
                });
            } finally {
                Platform.runLater(() -> {
                    playersPane.finishLoading();
                    controlPane.setScanning(false);
                });
            }
        }).start();
    }

    /** Return the root pane to embed in your scene. */
    public VBox getView() {
        return root;
    }
}
