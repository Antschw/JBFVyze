package fr.antschw.bfv.ui;

import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.application.util.constants.AppConstants;
import fr.antschw.bfv.ui.view.ServerView;
import fr.antschw.bfv.ui.view.SettingsView;
import fr.antschw.bfv.ui.view.StatsView;

import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * Controller for managing the main UI layout and view navigation in BFVyze.
 */
public class MainController {

    private final BorderPane root = new BorderPane();
    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final Button serverButton = new Button(bundle.getString("server.button"));
    private final Button statsButton = new Button(bundle.getString("stats.button"));
    private final Button settingsButton = new Button(bundle.getString("settings.button"));

    private final ServerView serverView;
    private final StatsView statsView;
    private final SettingsView settingsView;

    /**
     * Constructs the main controller with injected views.
     *
     * @param serverView   injected ServerView
     * @param statsView    injected StatsView
     * @param settingsView injected SettingsView
     */
    @Inject
    public MainController(ServerView serverView, StatsView statsView, SettingsView settingsView) {
        this.serverView = serverView;
        this.statsView = statsView;
        this.settingsView = settingsView;

        HBox navBar = createNavigationBar();
        Separator separator = createSeparator();

        VBox container = new VBox(navBar, separator);
        container.setSpacing(10);

        root.setTop(container);
        setView(serverView.getView()); // Initial view
    }

    private HBox createNavigationBar() {
        serverButton.setOnAction(e -> setView(serverView.getView()));
        statsButton.setOnAction(e -> setView(statsView.getView()));
        settingsButton.setOnAction(e -> setView(settingsView.getView()));

        serverButton.getStyleClass().add("button");
        statsButton.getStyleClass().add("button");
        settingsButton.getStyleClass().add("button");

        HBox navBar = new HBox(AppConstants.NAVBAR_SPACING, serverButton, statsButton, settingsButton);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(10, 0, 0, 0));
        return navBar;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        separator.setPadding(new Insets(10, 0, 0, 0));
        return separator;
    }

    private void setView(Node view) {
        root.setCenter(view);
    }

    public BorderPane getRoot() {
        return root;
    }
}
