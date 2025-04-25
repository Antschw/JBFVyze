package fr.antschw.bfv.adapters.input.window;

import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.utils.I18nUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;

/**
 * Pane that shows the “ready to scan” title and the scan button.
 */
public class ScanControlPane extends HBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final Button scanButton = new Button();
    private String activeHotkey;

    /**
     * @param hotkeyConfig service to read and listen for hotkey changes
     * @param onScan       callback to invoke when scan is requested
     */
    public ScanControlPane(HotkeyConfigurationService hotkeyConfig, Runnable onScan) {
        this.setSpacing(10);
        this.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label(bundle.getString("server.ready"));
        title.getStyleClass().add("header-label");

        activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
        updateButtonText();

        hotkeyConfig.setOnHotkeyUpdated(() -> {
            activeHotkey = hotkeyConfig.getConfiguration().getHotkey();
            updateButtonText();
        });

        scanButton.setOnAction(e -> onScan.run());

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
