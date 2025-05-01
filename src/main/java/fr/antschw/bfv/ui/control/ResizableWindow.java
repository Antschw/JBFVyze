package fr.antschw.bfv.ui.control;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitaire permettant d'ajouter la fonctionnalité de redimensionnement
 * à une fenêtre JavaFX sans décoration standard.
 */
public class ResizableWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResizableWindow.class);

    /**
     * Taille de la bordure de redimensionnement (invisible)
     */
    private static final int RESIZE_BORDER = 5;

    /**
     * La fenêtre (Stage) à rendre redimensionnable
     */
    private final Stage stage;

    /**
     * Indique si le redimensionnement est actuellement en cours dans une direction donnée
     */
    private boolean resizingN = false;
    private boolean resizingE = false;
    private boolean resizingS = false;
    private boolean resizingW = false;

    /**
     * Les coordonnées initiales de la souris lors du début du redimensionnement
     */
    private double startX;
    private double startY;

    /**
     * Les dimensions et positions initiales de la fenêtre lors du début du redimensionnement
     */
    private double startStageX;
    private double startStageY;
    private double startStageWidth;
    private double startStageHeight;

    /**
     * Crée un nouvel objet ResizableWindow pour la fenêtre spécifiée.
     *
     * @param stage La fenêtre à rendre redimensionnable
     */
    public ResizableWindow(Stage stage) {
        this.stage = stage;
    }

    /**
     * Active le redimensionnement de la fenêtre via le nœud racine spécifié.
     *
     * @param root Le nœud racine de la fenêtre, généralement un BorderPane
     */
    public void enableResize(Node root) {
        // Définir les gestionnaires d'événements souris
        root.setOnMouseMoved(this::handleMouseMoved);
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);
        root.setOnMouseReleased(this::handleMouseReleased);

        LOGGER.info("Window resize functionality enabled");
    }

    /**
     * Gère le déplacement de la souris pour afficher le curseur approprié.
     */
    private void handleMouseMoved(MouseEvent event) {
        // Ne pas modifier le curseur si la fenêtre est maximisée
        if (stage.isMaximized()) {
            return;
        }

        boolean atTop = event.getY() < RESIZE_BORDER;
        boolean atRight = event.getX() > stage.getWidth() - RESIZE_BORDER;
        boolean atBottom = event.getY() > stage.getHeight() - RESIZE_BORDER;
        boolean atLeft = event.getX() < RESIZE_BORDER;

        if (atTop && atLeft) {
            ((Node) event.getSource()).setCursor(Cursor.NW_RESIZE);
        } else if (atTop && atRight) {
            ((Node) event.getSource()).setCursor(Cursor.NE_RESIZE);
        } else if (atBottom && atRight) {
            ((Node) event.getSource()).setCursor(Cursor.SE_RESIZE);
        } else if (atBottom && atLeft) {
            ((Node) event.getSource()).setCursor(Cursor.SW_RESIZE);
        } else if (atTop) {
            ((Node) event.getSource()).setCursor(Cursor.N_RESIZE);
        } else if (atRight) {
            ((Node) event.getSource()).setCursor(Cursor.E_RESIZE);
        } else if (atBottom) {
            ((Node) event.getSource()).setCursor(Cursor.S_RESIZE);
        } else if (atLeft) {
            ((Node) event.getSource()).setCursor(Cursor.W_RESIZE);
        } else {
            ((Node) event.getSource()).setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Gère l'appui du bouton de la souris pour commencer le redimensionnement.
     */
    private void handleMousePressed(MouseEvent event) {
        // Ne pas permettre le redimensionnement si la fenêtre est maximisée
        if (stage.isMaximized()) {
            return;
        }

        startX = event.getScreenX();
        startY = event.getScreenY();
        startStageX = stage.getX();
        startStageY = stage.getY();
        startStageWidth = stage.getWidth();
        startStageHeight = stage.getHeight();

        boolean atTop = event.getY() < RESIZE_BORDER;
        boolean atRight = event.getX() > stage.getWidth() - RESIZE_BORDER;
        boolean atBottom = event.getY() > stage.getHeight() - RESIZE_BORDER;
        boolean atLeft = event.getX() < RESIZE_BORDER;

        resizingN = atTop;
        resizingE = atRight;
        resizingS = atBottom;
        resizingW = atLeft;
    }

    /**
     * Gère le glissement de la souris pour redimensionner la fenêtre.
     */
    private void handleMouseDragged(MouseEvent event) {
        // Ne pas permettre le redimensionnement si la fenêtre est maximisée
        if (stage.isMaximized()) {
            return;
        }

        if (!resizingN && !resizingE && !resizingS && !resizingW) {
            return;
        }

        double deltaX = event.getScreenX() - startX;
        double deltaY = event.getScreenY() - startY;

        double newX = startStageX;
        double newY = startStageY;
        double newWidth = startStageWidth;
        double newHeight = startStageHeight;

        // Redimensionnement Nord (haut)
        if (resizingN) {
            newY = startStageY + deltaY;
            newHeight = startStageHeight - deltaY;
        }

        // Redimensionnement Est (droite)
        if (resizingE) {
            newWidth = startStageWidth + deltaX;
        }

        // Redimensionnement Sud (bas)
        if (resizingS) {
            newHeight = startStageHeight + deltaY;
        }

        // Redimensionnement Ouest (gauche)
        if (resizingW) {
            newX = startStageX + deltaX;
            newWidth = startStageWidth - deltaX;
        }

        // Appliquer les nouvelles dimensions et position, avec des limites minimales
        if (newWidth >= stage.getMinWidth()) {
            stage.setWidth(newWidth);
            if (resizingW) {
                stage.setX(newX);
            }
        }

        if (newHeight >= stage.getMinHeight()) {
            stage.setHeight(newHeight);
            if (resizingN) {
                stage.setY(newY);
            }
        }
    }

    /**
     * Gère le relâchement du bouton de la souris pour terminer le redimensionnement.
     */
    private void handleMouseReleased(MouseEvent event) {
        resizingN = false;
        resizingE = false;
        resizingS = false;
        resizingW = false;

        // Rétablir le curseur par défaut si nous ne sommes pas sur une bordure
        handleMouseMoved(event);
    }
}