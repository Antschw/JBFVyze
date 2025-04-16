package fr.antschw.bfv.adapters.input.ui;

import fr.antschw.bfv.utils.I18nUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * View responsible for displaying stats-related information.
 */
public class StatsView {

    private final ResourceBundle bundle = I18nUtils.getBundle();

    /**
     * Constructs the StatsView layout.
     *
     * @return VBox containing stats information
     */
    public VBox getView() {
        Label statsLabel = new Label(bundle.getString("stats.title").replace("{0}", "Player"));
        statsLabel.getStyleClass().add("label");

        VBox box = new VBox(statsLabel);
        box.setSpacing(15);
        box.setPadding(new Insets(20, 0, 0, 20));
        return box;
    }
}
