package fr.antschw.bfv.ui.control;

import fr.antschw.bfv.application.util.AppConstants;
import fr.antschw.bfv.infrastructure.window.TitleBarMetrics;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Barre de titre custom (style IntelliJ) dessinée en JavaFX et étendue dans la
 * zone de caption Windows par {@code WindowsTitleBarDecoration}.
 * <p>
 * Layout : {@code [BFVyze] [ ... nav ... ] [thème] [— □ ✕]}.
 * <p>
 * Aucun handler de drag ou de double-clic ici : quand la décoration native est
 * active, Windows gère lui-même le déplacement (HTCAPTION), le double-clic
 * maximise, Aero Snap et les Snap Layouts via le hit-test enregistré dans
 * {@link TitleBarMetrics}. Les clics/survols des boutons caption arrivent par
 * les callbacks natifs, pas par les événements souris JavaFX.
 */
public class NativeTitleBar extends HBox {

    private static final PseudoClass HOVER_NATIVE = PseudoClass.getPseudoClass("hover-native");
    private static final double CAPTION_BUTTON_WIDTH = 46;

    private final Button minimizeButton;
    private final Button maximizeButton;
    private final Button closeButton;
    private final HBox captionButtons;

    private Stage stage;

    /**
     * @param metrics    registre des zones partagé avec la couche native
     * @param navigation zone de navigation (reste interactive côté JavaFX)
     * @param themeToggle bouton de bascule de thème (reste interactif côté JavaFX)
     */
    public NativeTitleBar(TitleBarMetrics metrics, Node navigation, Node themeToggle) {
        getStyleClass().add("native-title-bar");
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(AppConstants.TITLE_BAR_HEIGHT);
        setPrefHeight(AppConstants.TITLE_BAR_HEIGHT);
        setMaxHeight(AppConstants.TITLE_BAR_HEIGHT);
        setPadding(new Insets(0, 0, 0, 12));

        Label title = new Label("BFVyze");
        title.getStyleClass().add("title-label");

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        minimizeButton = createCaptionButton("mdi2w-window-minimize");
        maximizeButton = createCaptionButton("mdi2w-window-maximize");
        closeButton = createCaptionButton("mdi2c-close");
        closeButton.getStyleClass().add("caption-button-close");

        // Fallback JavaFX : ne sert que si un clic atteint la zone client
        // (décoration native inactive ou hit-test raté).
        minimizeButton.setOnAction(e -> runAction(TitleBarMetrics.HTMINBUTTON));
        maximizeButton.setOnAction(e -> runAction(TitleBarMetrics.HTMAXBUTTON));
        closeButton.setOnAction(e -> runAction(TitleBarMetrics.HTCLOSE));

        captionButtons = new HBox(minimizeButton, maximizeButton, closeButton);
        captionButtons.setAlignment(Pos.CENTER_RIGHT);
        // Masqués tant que la décoration native n'est pas installée : la barre
        // de titre système fournit alors ses propres boutons.
        setNativeDecorationActive(false);

        getChildren().addAll(title, leftSpacer, navigation, rightSpacer, themeToggle, captionButtons);

        metrics.setTitleBar(this);
        metrics.registerCaptionButton(TitleBarMetrics.HTMINBUTTON, minimizeButton);
        metrics.registerCaptionButton(TitleBarMetrics.HTMAXBUTTON, maximizeButton);
        metrics.registerCaptionButton(TitleBarMetrics.HTCLOSE, closeButton);
        metrics.registerClientZone(navigation);
        metrics.registerClientZone(themeToggle);
        metrics.setOnHover(this::applyHover);
        metrics.setOnAction(this::runAction);
    }

    /**
     * Lie la barre au Stage (actions min/max/close, icône maximise/restaure).
     * À appeler avant {@code stage.show()}.
     */
    public void attachStage(Stage stage) {
        this.stage = stage;
        stage.maximizedProperty().addListener((obs, was, maximized) ->
                setCaptionIcon(maximizeButton, maximized ? "mdi2w-window-restore" : "mdi2w-window-maximize"));
    }

    /** Affiche les boutons caption custom quand la décoration native est installée. */
    public void setNativeDecorationActive(boolean active) {
        captionButtons.setVisible(active);
        captionButtons.setManaged(active);
    }

    private void applyHover(int hitTestCode) {
        minimizeButton.pseudoClassStateChanged(HOVER_NATIVE, hitTestCode == TitleBarMetrics.HTMINBUTTON);
        maximizeButton.pseudoClassStateChanged(HOVER_NATIVE, hitTestCode == TitleBarMetrics.HTMAXBUTTON);
        closeButton.pseudoClassStateChanged(HOVER_NATIVE, hitTestCode == TitleBarMetrics.HTCLOSE);
    }

    private void runAction(int hitTestCode) {
        if (stage == null) {
            return;
        }
        switch (hitTestCode) {
            case TitleBarMetrics.HTMINBUTTON -> stage.setIconified(true);
            case TitleBarMetrics.HTMAXBUTTON -> stage.setMaximized(!stage.isMaximized());
            case TitleBarMetrics.HTCLOSE -> stage.close();
            default -> { }
        }
    }

    private Button createCaptionButton(String iconLiteral) {
        Button button = new Button();
        button.getStyleClass().add("caption-button");
        button.setFocusTraversable(false);
        button.setMinSize(CAPTION_BUTTON_WIDTH, AppConstants.TITLE_BAR_HEIGHT);
        button.setPrefSize(CAPTION_BUTTON_WIDTH, AppConstants.TITLE_BAR_HEIGHT);
        button.setMaxSize(CAPTION_BUTTON_WIDTH, AppConstants.TITLE_BAR_HEIGHT);
        setCaptionIcon(button, iconLiteral);
        return button;
    }

    private static void setCaptionIcon(Button button, String iconLiteral) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        button.setGraphic(icon);
    }
}
