package fr.antschw.bfv.adapters.input.ui;

import fr.antschw.bfv.application.service.ServerScanService;
import fr.antschw.bfv.domain.model.InterestingPlayer;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.domain.service.HotkeyListenerException;
import fr.antschw.bfv.infrastructure.hotkey.JNativeHookHotkeyListenerAdapter;
import fr.antschw.bfv.utils.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

/**
 * JavaFX view for scanning Battlefield V servers and displaying player analysis.
 */
public class ServerView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerView.class);
    private final VBox view = new VBox(15);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final ServerScanService scanService;
    private final HotkeyConfigurationService hotkeyConfig;
    private final HotkeyListenerService hotkeyListener;

    private final Label ocrStatusLabel = new Label();
    private final Label gameToolsStatusLabel = new Label();
    private final Label hackersStatusLabel = new Label();
    private final Label timeLabel = new Label();

    private final ProgressIndicator ocrSpinner = new ProgressIndicator();
    private final ProgressIndicator gtSpinner = new ProgressIndicator();
    private final ProgressIndicator hackersSpinner = new ProgressIndicator();

    private final HBox ocrBox = new HBox(10);
    private final HBox gtBox = new HBox(10);
    private final HBox hackersBox = new HBox(10);

    private final VBox flaggedPlayersBox = new VBox(8);
    private final ProgressBar playerProgressBar = new ProgressBar();

    private final Button scanButton = new Button();
    private final Label titleLabel = new Label();
    private final Label playersTitle = new Label();

    private String activeHotkey;

    /**
     * Constructs the view with injected services.
     */
    @Inject
    public ServerView(ServerScanService scanService, HotkeyConfigurationService hotkeyConfig, HotkeyListenerService hotkeyListener) {
        this.scanService = scanService;
        this.hotkeyConfig = hotkeyConfig;
        this.hotkeyListener = hotkeyListener;
        this.view.setPadding(new Insets(20));
        this.view.getStyleClass().add("overlay");
        this.view.setOnKeyPressed(this::handleKeyPress);
        
        try {
            hotkeyListener.startListening(() -> Platform.runLater(this::runScan));
        } catch (HotkeyListenerException e) {
            LOGGER.error("Failed to initialize hotkey listener", e);
        }
        
        initLayout();
    }

    private void initLayout() {
        // Text headers
        titleLabel.setText(bundle.getString("server.ready"));
        titleLabel.getStyleClass().add("header-label");

        playersTitle.setText(bundle.getString("server.result.players"));
        playersTitle.getStyleClass().add("header-label");

        // Button
        updateScanButtonText();
        hotkeyConfig.setOnHotkeyUpdated(() -> {
            Platform.runLater(() -> {
                updateScanButtonText();
                activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
            });
        });
        scanButton.setOnAction(e -> runScan());

        // Spinner setup
        ocrSpinner.setPrefSize(16, 16);
        gtSpinner.setPrefSize(16, 16);
        hackersSpinner.setPrefSize(16, 16);
        ocrSpinner.setVisible(false);
        gtSpinner.setVisible(false);
        hackersSpinner.setVisible(false);

        // Status layout
        ocrBox.getChildren().addAll(ocrSpinner, ocrStatusLabel);
        gtBox.getChildren().addAll(gtSpinner, gameToolsStatusLabel);
        hackersBox.getChildren().addAll(hackersSpinner, hackersStatusLabel);

        // Progress bar for player stats
        playerProgressBar.setPrefWidth(300);
        playerProgressBar.setVisible(false);
        playerProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        view.getChildren().addAll(
                titleLabel,
                scanButton,
                ocrBox,
                gtBox,
                hackersBox,
                timeLabel,
                new Separator(),
                playersTitle,
                playerProgressBar,
                flaggedPlayersBox
        );
    }

    private void updateScanButtonText() {
        activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
        scanButton.setText(bundle.getString("server.press").replace("{0}", activeHotkey));
    }

    private void handleKeyPress(KeyEvent event) {
        if (activeHotkey == null) return;
        String pressed = event.getCode().getName();
        if (pressed.equalsIgnoreCase(activeHotkey)) {
            runScan();
        }
    }

    private void runScan() {
        scanButton.setDisable(true);
        resetState();

        Instant start = Instant.now();

        new Thread(() -> {
            try {
                String shortId = scanService.extractServerId();
                Platform.runLater(() -> {
                    ocrSpinner.setVisible(false);
                    ocrStatusLabel.setText(bundle.getString("server.result.ocr").replace("{0}", shortId));
                });

                var info = scanService.queryGameTools(shortId);
                String longId = String.valueOf(info.getLongServerId());
                Platform.runLater(() -> {
                    gtSpinner.setVisible(false);
                    gameToolsStatusLabel.setText(bundle.getString("server.result.gametools").replace("{0}", longId));
                });

                var combined = scanService.queryBfvHackers(longId, info);
                Platform.runLater(() -> {
                    hackersSpinner.setVisible(false);
                    hackersStatusLabel.setText(bundle.getString("server.result.hackers")
                            .replace("{0}", String.valueOf(combined.getCheaterCount())));
                });

                Platform.runLater(() -> {
                    playerProgressBar.setVisible(true);
                    flaggedPlayersBox.getChildren().clear();
                });

                List<InterestingPlayer> flagged = scanService.queryPlayers(shortId, player -> {
                    Platform.runLater(() -> {
                        Label label = new Label(player.toString());
                        label.getStyleClass().add("label");
                        flaggedPlayersBox.getChildren().add(label);
                    });
                });

                Platform.runLater(() -> playerProgressBar.setVisible(false));

                Duration duration = Duration.between(start, Instant.now());
                String seconds = String.format("%.1f", duration.toMillis() / 1000.0);
                Platform.runLater(() ->
                        timeLabel.setText(bundle.getString("server.result.time").replace("{0}", seconds + "s"))
                );

            } catch (Exception e) {
                Platform.runLater(() -> {
                    ocrSpinner.setVisible(false);
                    gtSpinner.setVisible(false);
                    hackersSpinner.setVisible(false);
                    ocrStatusLabel.setText(bundle.getString("server.result.error").replace("{0}", e.getMessage()));
                    gameToolsStatusLabel.setText(bundle.getString("server.result.aborted"));
                    hackersStatusLabel.setText(bundle.getString("server.result.aborted"));
                    timeLabel.setText(bundle.getString("server.result.failure"));
                    playerProgressBar.setVisible(false);
                    scanButton.setDisable(false);
                });
            }
        }).start();
    }

    private void resetState() {
        ocrStatusLabel.setText(bundle.getString("server.ocr.running"));
        gameToolsStatusLabel.setText(bundle.getString("server.gametools.waiting"));
        hackersStatusLabel.setText(bundle.getString("server.hackers.waiting"));
        timeLabel.setText("");
        flaggedPlayersBox.getChildren().clear();

        ocrSpinner.setVisible(true);
        gtSpinner.setVisible(true);
        hackersSpinner.setVisible(true);
    }

    public VBox getView() {
        return view;
    }
}
