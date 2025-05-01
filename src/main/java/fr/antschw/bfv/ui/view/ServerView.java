package fr.antschw.bfv.ui.view;

import fr.antschw.bfv.application.orchestrator.PlayerStatsFilter;
import fr.antschw.bfv.application.orchestrator.ServerScanCoordinator;
import fr.antschw.bfv.application.util.AppConstants;
import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.domain.exception.HotkeyListenerException;
import fr.antschw.bfv.domain.model.HackersSummary;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersClient;
import fr.antschw.bfv.ui.component.TimerComponent;
import fr.antschw.bfv.ui.panel.PlayersPanel;
import fr.antschw.bfv.ui.panel.ScanControlPanel;
import fr.antschw.bfv.ui.panel.StatusPanel;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Orchestrates the scan flow by delegating to three panels:
 * ScanControlPanel, StatusPanel, and PlayersPanel.
 * Maintenant avec un timer pour mesurer précisément le temps écoulé.
 */
public class ServerView {

    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final VBox root = new VBox(8); // Espacement réduit entre composants
    private final ServerScanCoordinator scanService;
    private final PlayerStatsFilter playerStatsFilter;
    private final ScanControlPanel controlPane;
    private final StatusPanel statusPane = new StatusPanel();
    private final PlayersPanel playersPane = new PlayersPanel();
    private final TimerComponent scanTimer = new TimerComponent();

    @Inject
    public ServerView(ServerScanCoordinator scanService,
                      HotkeyConfigurationService hotkeyConfig,
                      HotkeyListenerService hotkeyListener,
                      PlayerStatsFilter playerStatsFilter) {
        this.scanService = scanService;
        this.playerStatsFilter = playerStatsFilter;

        controlPane = new ScanControlPanel(hotkeyConfig, this::runScan);
        root.setPadding(new Insets(AppConstants.WINDOW_PADDING));

        // Configurer le timer
        scanTimer.getStyleClass().add("timer-component");

        // Connecter le timer au panel des joueurs
        playersPane.setTimeLabel(scanTimer);
        statusPane.setTimeLabel(scanTimer);

        // Ajout des composants avec séparateur plus mince
        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 2, 0)); // Réduire l'espace vertical

        root.getChildren().addAll(controlPane, statusPane, separator, playersPane);

        // Le PlayersPanel doit être le seul à s'étendre
        VBox.setVgrow(playersPane, Priority.ALWAYS);
        VBox.setVgrow(statusPane, Priority.NEVER);
        VBox.setVgrow(controlPane, Priority.NEVER);

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

        // Démarrer le timer
        scanTimer.reset();
        scanTimer.start();

        new Thread(() -> {
            try {
                // 1) OCR
                String shortId = scanService.extractServerId();
                Platform.runLater(() -> {
                    statusPane.addToHistory(shortId);
                    statusPane.setOcrStatus("#" + shortId, false);
                });


                // 2) GameTools
                var info = scanService.queryGameTools(shortId);
                Platform.runLater(() ->
                        statusPane.setGameToolsStatus("ID " + info.longServerId(), false)
                );

                // 3) Hackers
                var hackInfo = scanService.queryBfvHackers(String.valueOf(info.longServerId()), info);

                // Récupérer le résumé complet des hackers depuis le client
                HackersSummary summary = BfvHackersClient.getLatestSummary();

                Platform.runLater(() -> {
                    if (summary != null) {
                        // Utiliser la nouvelle méthode avec le résumé complet
                        statusPane.setHackersStatus(summary, false);
                    } else {
                        // Fallback: créer un résumé simplifié
                        statusPane.setHackersStatus(
                                new HackersSummary(
                                        0, // total inconnu
                                        0, // legit inconnu
                                        0, // sus inconnu
                                        0, // very sus inconnu
                                        hackInfo.cheaterCount(), // nombre de hackers
                                        0  // âge inconnu
                                ),
                                false
                        );
                    }
                });

                // 4) Players async
                scanService.queryPlayersAsync(
                        shortId,
                        player -> Platform.runLater(() ->
                                playersPane.addPlayer(player.name(), player.playerId())
                        ),
                        (player, stats) -> Platform.runLater(() -> {
                            // Get the list of metrics that flagged this player
                            List<String> metrics = stats != null
                                    ? playerStatsFilter.getInterestingMetrics(stats)
                                    : List.of();

                            playersPane.updatePlayer(
                                    player.name(),
                                    stats != null ? stats.killDeath() : null,
                                    stats != null ? stats.killsPerMinute() : null,
                                    stats != null ? stats.rank() : null,
                                    stats != null ? stats.accuracy() : null,
                                    metrics
                            );
                        })
                );

                // Arrêter le timer à la fin du scan
                Platform.runLater(() -> {
                    scanTimer.stop();
                    playersPane.finishLoading();
                    controlPane.setScanning(false);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusPane.setOcrStatus("Error: " + ex.getMessage(), false);
                    statusPane.setGameToolsStatus("", false);
                    statusPane.setHackersStatus(
                            new HackersSummary(0, 0, 0, 0, 0, 0),
                            false
                    );
                    scanTimer.stop();
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