package fr.antschw.bfv.ui.control;

import fr.antschw.bfv.application.util.I18nUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Contrôleur pour la fenêtre principale de l'application.
 * Gère la barre de titre personnalisée et les comportements de fenêtre standard.
 */
public class MainWindowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindowController.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private final BorderPane rootPane = new BorderPane();
    private final StringProperty currentViewTitle = new SimpleStringProperty("BFVyze");

    private final Label titleLabel = new Label();
    private boolean darkTheme = false;

    private Button themeButton;
    private Button maximizeButton;

    /**
     * Initialise la fenêtre principale.
     */
    public void initialize(Stage stage) {
        LOGGER.info("Initializing main window controller");

        try {
            // Configurer la barre de titre
            HBox titleBar = createTitleBar(stage);
            rootPane.setTop(titleBar);

            // Mise à jour du titre lorsqu'il change
            currentViewTitle.addListener((obs, oldVal, newVal) -> {
                titleLabel.setText("BFVyze - " + newVal);
            });

            // Ajouter le support pour le redimensionnement de la fenêtre
            ResizableWindow resizableWindow = new ResizableWindow(stage);
            resizableWindow.enableResize(rootPane);

            // Définir un style pour la bordure de la fenêtre
            rootPane.getStyleClass().add("window-root");

            LOGGER.info("Main window controller initialized");
        } catch (Exception e) {
            LOGGER.error("Error initializing main window controller", e);
            throw new RuntimeException("Failed to initialize main window controller", e);
        }
    }

    /**
     * Crée la barre de titre avec les boutons standard Windows.
     */
    private HBox createTitleBar(Stage stage) {
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.setPadding(new Insets(6, 10, 6, 10));

        // Titre de l'application
        titleLabel.setText("BFVyze");
        titleLabel.getStyleClass().add("title-label");

        // Espace flexible
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons de fenêtre
        themeButton = createThemeButton();
        Button minimizeButton = createWindowButton("mdi2w-window-minimize", e -> stage.setIconified(true));
        maximizeButton = createWindowButton("mdi2w-window-maximize", e -> maximizeRestore(stage));
        Button closeButton = createWindowButton("mdi2c-close", e -> stage.close());

        // Ajout des composants
        titleBar.getChildren().addAll(
                titleLabel,
                spacer,
                themeButton,
                minimizeButton,
                maximizeButton,
                closeButton);

        // Ajout de la possibilité de déplacer la fenêtre via la barre de titre
        enableWindowDrag(stage, titleBar);

        // Double-clic sur la barre de titre pour maximiser/restaurer
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                maximizeRestore(stage);
            }
        });

        return titleBar;
    }

    /**
     * Active la possibilité de déplacer la fenêtre en faisant glisser la barre de titre.
     */
    private void enableWindowDrag(Stage stage, HBox titleBar) {
        // Variables pour le déplacement de la fenêtre
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];

        titleBar.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            }
        });
    }

    /**
     * Crée un bouton pour la barre de titre.
     */
    private Button createWindowButton(String iconLiteral, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("window-button");
        button.setOnAction(action);

        return button;
    }

    /**
     * Crée un bouton pour basculer entre le thème clair et sombre.
     */
    private Button createThemeButton() {
        FontIcon icon = new FontIcon("mdi2w-weather-night");
        icon.setIconSize(14);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("window-button");

        button.setOnAction(e -> toggleTheme());

        return button;
    }

    /**
     * Bascule entre le thème clair et sombre.
     */
    private void toggleTheme() {
        darkTheme = !darkTheme;

        if (darkTheme) {
            // Appliquer thème sombre
            javafx.application.Application.setUserAgentStylesheet(
                    new atlantafx.base.theme.Dracula().getUserAgentStylesheet());

            // Mettre à jour l'icône du bouton
            FontIcon icon = new FontIcon("mdi2w-weather-sunny");
            icon.setIconSize(14);
            themeButton.setGraphic(icon);
        } else {
            // Appliquer thème clair
            javafx.application.Application.setUserAgentStylesheet(
                    new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());

            // Mettre à jour l'icône du bouton
            FontIcon icon = new FontIcon("mdi2w-weather-night");
            icon.setIconSize(14);
            themeButton.setGraphic(icon);
        }
    }

    /**
     * Active/désactive l'état maximisé de la fenêtre.
     */
    private void maximizeRestore(Stage stage) {
        if (stage.isMaximized()) {
            stage.setMaximized(false);

            // Mettre à jour l'icône
            FontIcon icon = new FontIcon("mdi2w-window-maximize");
            icon.setIconSize(14);
            maximizeButton.setGraphic(icon);
        } else {
            stage.setMaximized(true);

            // Mettre à jour l'icône
            FontIcon icon = new FontIcon("mdi2w-window-restore");
            icon.setIconSize(14);
            maximizeButton.setGraphic(icon);
        }
    }

    /**
     * Définit le contenu principal de la fenêtre.
     */
    public void setContent(Node content) {
        rootPane.setCenter(content);
    }

    /**
     * Met à jour le titre de la vue actuelle.
     */
    public void setViewTitle(String title) {
        currentViewTitle.set(title);
    }

    /**
     * Récupère le panneau racine.
     */
    public BorderPane getRoot() {
        return rootPane;
    }
}