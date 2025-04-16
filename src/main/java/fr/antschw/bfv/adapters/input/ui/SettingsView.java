package fr.antschw.bfv.adapters.input.ui;

import com.google.inject.Inject;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.utils.I18nUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * JavaFX view for configuring the scan hotkey.
 */
public class SettingsView {

    private final VBox view = new VBox(15);
    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final HotkeyConfigurationService configurationService;

    private final Label currentKeyLabel = new Label();
    private final TextField keyCaptureField = new TextField();
    private final Button saveButton = new Button();

    /**
     * Constructs the SettingsView with injected hotkey service.
     */
    @Inject
    public SettingsView(HotkeyConfigurationService configurationService) {
        this.configurationService = configurationService;
        initLayout();
    }

    private void initLayout() {
        view.setPadding(new Insets(20));

        Label title = new Label(bundle.getString("settings.title"));
        title.getStyleClass().add("label");

        Label instructionLabel = new Label(bundle.getString("settings.screenshot"));
        instructionLabel.getStyleClass().add("label");

        updateCurrentKeyLabel();

        keyCaptureField.setPromptText(bundle.getString("settings.capture.prompt"));
        keyCaptureField.setEditable(false);
        keyCaptureField.setFocusTraversable(true);
        keyCaptureField.getStyleClass().add("label");

        keyCaptureField.setOnKeyPressed(this::handleKeyPressed);

        saveButton.setText(bundle.getString("settings.save"));
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> saveKey());

        view.getChildren().addAll(title, instructionLabel, currentKeyLabel, keyCaptureField, saveButton);
    }

    private void updateCurrentKeyLabel() {
        String hotkey = configurationService.getConfiguration().getHotkey();
        currentKeyLabel.setText(bundle.getString("settings.current").replace("{0}", hotkey));
        currentKeyLabel.getStyleClass().add("label");
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != null) {
            keyCaptureField.setText(code.getName());
            saveButton.setDisable(false);
        }
    }

    private void saveKey() {
        String newHotkey = keyCaptureField.getText();
        configurationService.updateConfiguration(newHotkey);
        updateCurrentKeyLabel();
        saveButton.setDisable(true);
    }

    /**
     * Returns the root node for this view.
     *
     * @return VBox view
     */
    public VBox getView() {
        return view;
    }
}
