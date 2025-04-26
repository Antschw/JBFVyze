package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.application.util.I18nUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ResourceBundle;

/**
 * Pane that shows the “ready to scan” title and the scan button,
 * with the button aligned to the right.
 */
public class ScanControlPanel extends HBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final Button scanButton;
    private String activeHotkey;

    public ScanControlPanel(HotkeyConfigurationService hotkeyConfig, Runnable onScan) {
        this.setSpacing(10);
        this.setPadding(new Insets(10, 0, 10, 0));

        // 1) Title label, will grow horizontally to push the button right
        Label title = new Label(bundle.getString("server.ready"));
        title.getStyleClass().add("header-label");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        // 2) Scan button with dynamic hotkey
        scanButton = new Button();
        activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
        updateButtonText();

        hotkeyConfig.setOnHotkeyUpdated(() -> {
            activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
            updateButtonText();
        });

        scanButton.setOnAction(e -> onScan.run());

        // 3) Add to HBox: title takes all the space, button sticks right
        this.getChildren().addAll(title, scanButton);
    }

    private void updateButtonText() {
        String template = bundle.getString("server.press");
        scanButton.setText(template.replace("{0}", activeHotkey));
    }

    /** Disable the scan button while scanning is in progress. */
    public void setScanning(boolean scanning) {
        scanButton.setDisable(scanning);
    }
}
