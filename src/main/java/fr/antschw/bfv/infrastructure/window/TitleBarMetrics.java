package fr.antschw.bfv.infrastructure.window;

import javafx.geometry.Bounds;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Pont entre la barre de titre JavaFX et la couche native Win32.
 * <p>
 * La barre JavaFX ({@code NativeTitleBar}) enregistre ici ses zones : boutons
 * caption (min/max/close) associés à leur code de hit-test Win32, et zones
 * interactives (navigation, toggle de thème) qui doivent rester cliquables
 * côté JavaFX. La couche native ({@code WindowsTitleBarDecoration}) interroge
 * {@link #hitTest(double, double)} depuis la WndProc.
 * <p>
 * Aucune dépendance JNA ici : seuls les codes de hit-test Win32 sont repris
 * comme constantes. Toutes les coordonnées sont logiques (coordonnées scène
 * JavaFX). Les appels se font exclusivement sur le FX Application Thread
 * (la WndProc s'exécute sur ce thread sous Windows).
 */
public final class TitleBarMetrics {

    /** Codes de hit-test Win32 (WinUser.h). */
    public static final int HTNOWHERE = 0;
    public static final int HTCLIENT = 1;
    public static final int HTCAPTION = 2;
    public static final int HTMINBUTTON = 8;
    public static final int HTMAXBUTTON = 9;
    public static final int HTCLOSE = 20;

    private Node titleBar;
    private final Map<Integer, Node> captionButtons = new LinkedHashMap<>();
    private final List<Node> clientZones = new ArrayList<>();

    private IntConsumer onHover = ht -> { };
    private IntConsumer onAction = ht -> { };

    /**
     * Déclare le nœud couvrant toute la barre de titre. Tout point de ce nœud
     * qui n'est ni un bouton caption ni une zone interactive devient HTCAPTION
     * (drag natif, double-clic maximise, Aero Snap).
     */
    public void setTitleBar(Node titleBar) {
        this.titleBar = titleBar;
    }

    /**
     * Enregistre un bouton caption dessiné en JavaFX.
     *
     * @param htCode {@link #HTMINBUTTON}, {@link #HTMAXBUTTON} ou {@link #HTCLOSE}
     * @param node   le bouton JavaFX correspondant
     */
    public void registerCaptionButton(int htCode, Node node) {
        captionButtons.put(htCode, node);
    }

    /**
     * Enregistre une zone de la barre de titre qui doit rester interactive
     * côté JavaFX (boutons de navigation, toggle de thème…).
     */
    public void registerClientZone(Node node) {
        clientZones.add(node);
    }

    /** Callback hover : reçoit le code HT survolé, ou {@link #HTNOWHERE}. */
    public void setOnHover(IntConsumer onHover) {
        this.onHover = onHover;
    }

    /** Callback action : reçoit le code HT du bouton caption cliqué. */
    public void setOnAction(IntConsumer onAction) {
        this.onAction = onAction;
    }

    /**
     * Hit-test en coordonnées scène logiques. Appelé depuis la WndProc
     * (FX Application Thread) pour chaque WM_NCHITTEST.
     *
     * @return le code HT à retourner à Windows pour ce point
     */
    public int hitTest(double sceneX, double sceneY) {
        for (Map.Entry<Integer, Node> entry : captionButtons.entrySet()) {
            if (contains(entry.getValue(), sceneX, sceneY)) {
                return entry.getKey();
            }
        }
        for (Node zone : clientZones) {
            if (contains(zone, sceneX, sceneY)) {
                return HTCLIENT;
            }
        }
        if (titleBar != null && contains(titleBar, sceneX, sceneY)) {
            return HTCAPTION;
        }
        return HTCLIENT;
    }

    void fireHover(int htCode) {
        onHover.accept(htCode);
    }

    void fireAction(int htCode) {
        onAction.accept(htCode);
    }

    private static boolean contains(Node node, double sceneX, double sceneY) {
        if (node == null || !node.isVisible() || node.getScene() == null) {
            return false;
        }
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return bounds != null && bounds.contains(sceneX, sceneY);
    }
}
