package fr.antschw.bfv.adapters.input.window;

import fr.antschw.bfv.utils.I18nUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;

import java.util.ResourceBundle;

/**
 * Panel that displays OCR, GameTools, Hackers and elapsed-time statuses.
 */
public class StatusPanel extends GridPane {

    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final ProgressIndicator ocrSpinner = new ProgressIndicator();
    private final Label ocrLabel = new Label();

    private final ProgressIndicator gtSpinner = new ProgressIndicator();
    private final Label gtLabel = new Label();

    private final ProgressIndicator hkSpinner = new ProgressIndicator();
    private final Label hkLabel = new Label();

    private final Label timeLabel = new Label();

    public StatusPanel() {
        this.setHgap(10);
        this.setVgap(6);
        this.setPadding(new Insets(0, 0, 10, 0));

        // OCR row
        ocrSpinner.setPrefSize(16,16);
        ocrSpinner.setVisible(false);
        ocrLabel.getStyleClass().add("scan-label");
        this.addRow(0, ocrSpinner, ocrLabel);

        // GameTools row
        gtSpinner.setPrefSize(16,16);
        gtSpinner.setVisible(false);
        gtLabel.getStyleClass().add("scan-label");
        this.addRow(1, gtSpinner, gtLabel);

        // Hackers row
        hkSpinner.setPrefSize(16,16);
        hkSpinner.setVisible(false);
        hkLabel.getStyleClass().add("scan-label");
        this.addRow(2, hkSpinner, hkLabel);

        // Time row
        timeLabel.getStyleClass().add("time-label");
        this.addRow(3, new Label(), timeLabel);
    }

    /** Call when OCR starts or finishes. */
    public void setOcrStatus(String text, boolean running) {
        ocrSpinner.setVisible(running);
        ocrLabel.setText(text);
    }

    /** Call when GameTools query status updates. */
    public void setGameToolsStatus(String text, boolean running) {
        gtSpinner.setVisible(running);
        gtLabel.setText(text);
    }

    /** Call when BFVHackers status updates; if hasCheaters add danger style. */
    public void setHackersStatus(String text, boolean running, boolean hasCheaters) {
        hkSpinner.setVisible(running);
        hkLabel.setText(text);
        hkLabel.getStyleClass().removeAll("scan-success","scan-danger");
        hkLabel.getStyleClass().add(hasCheaters ? "scan-danger" : "scan-success");
    }

    /** Call to update total elapsed time. */
    public void setTime(String text) {
        timeLabel.setText(text);
    }

    /** Clear all status fields and show spinners. */
    public void reset() {
        setOcrStatus(bundle.getString("server.ocr.running"), true);
        setGameToolsStatus(bundle.getString("server.gametools.waiting"), true);
        setHackersStatus(bundle.getString("server.hackers.waiting"), true, false);
        setTime("");
    }
}
