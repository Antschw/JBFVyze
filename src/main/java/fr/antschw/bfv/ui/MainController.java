package fr.antschw.bfv.ui;

import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.application.util.AppConstants;
import fr.antschw.bfv.ui.control.ThemeToggleButton;
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
        setView(serverView.getView(), bundle.getString("server.button")); // Initial view
    }

    private HBox createNavigationBar() {
        serverButton.setOnAction(e -> setView(serverView.getView(), bundle.getString("server.button")));
        statsButton.setOnAction(e -> setView(statsView.getView(), bundle.getString("stats.button")));
        settingsButton.setOnAction(e -> setView(settingsView.getView(), bundle.getString("settings.button")));

        serverButton.getStyleClass().add("nav-button");
        statsButton.getStyleClass().add("nav-button");
        settingsButton.getStyleClass().add("nav-button");

        // Bouton de bascule de thème
        ThemeToggleButton themeButton = new ThemeToggleButton();
        themeButton.getStyleClass().add("theme-button");

        // Créer un conteneur flexible pour centrer les boutons de navigation
        // et placer le bouton de thème à droite
        HBox centerButtons = new HBox(AppConstants.NAVBAR_SPACING, serverButton, statsButton, settingsButton);
        centerButtons.setAlignment(Pos.CENTER);

        // Spacer pour pousser le bouton de thème à droite
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox navBar = new HBox(10, centerButtons, spacer, themeButton);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(10, 10, 0, 10));
        return navBar;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        separator.setPadding(new Insets(5, 0, 0, 0));
        return separator;
    }

    private void setView(Node view, String title) {
        root.setCenter(view);

        // Mettre à jour l'état actif des boutons de navigation
        updateActiveButton(title);

        // Mettre à jour le titre de la fenêtre si on voulait afficher le nom de la vue
        // Stage stage = (Stage) root.getScene().getWindow();
        // if (stage != null) {
        //     stage.setTitle("BFVyze - " + title);
        // }
    }

    /**
     * Met à jour l'état actif des boutons de navigation.
     *
     * @param activeTitle le titre de la vue active
     */
    private void updateActiveButton(String activeTitle) {
        // Retirer la classe active de tous les boutons
        serverButton.getStyleClass().remove("active");
        statsButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");

        // Ajouter la classe active au bouton correspondant à la vue active
        if (activeTitle.equals(bundle.getString("server.button"))) {
            serverButton.getStyleClass().add("active");
        } else if (activeTitle.equals(bundle.getString("stats.button"))) {
            statsButton.getStyleClass().add("active");
        } else if (activeTitle.equals(bundle.getString("settings.button"))) {
            settingsButton.getStyleClass().add("active");
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}