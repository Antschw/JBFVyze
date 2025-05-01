package fr.antschw.bfv.ui.component;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Composant d'affichage du temps écoulé avec minutes et secondes.
 * Peut être utilisé dans différentes vues pour afficher la durée d'une session.
 */
public class TimerComponent extends Label {

    private Instant startTime;
    private Timeline timeline;
    private boolean running = false;

    /**
     * Constructeur avec initialisation du composant.
     */
    public TimerComponent() {
        this.getStyleClass().add("time-display");
        setText("0m 00s");
        configureTimeline();
    }

    /**
     * Configure la timeline pour mettre à jour l'affichage à intervalles réguliers.
     */
    private void configureTimeline() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> updateDisplay())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Met à jour l'affichage du timer en fonction du temps écoulé.
     */
    private void updateDisplay() {
        if (startTime != null) {
            java.time.Duration elapsed = java.time.Duration.between(startTime, Instant.now());
            long minutes = elapsed.toMinutes();
            long seconds = elapsed.minusMinutes(minutes).getSeconds();
            setText(String.format("%dm %02ds", minutes, seconds));
        }
    }

    /**
     * Démarre le timer à partir de l'instant spécifié.
     *
     * @param startInstant L'instant de départ
     */
    public void start(Instant startInstant) {
        this.startTime = startInstant;
        this.running = true;
        timeline.play();
    }

    /**
     * Démarre le timer à partir de l'instant présent.
     */
    public void start() {
        start(Instant.now());
    }

    /**
     * Arrête le timer.
     */
    public void stop() {
        timeline.stop();
        this.running = false;
    }

    /**
     * Réinitialise le timer à zéro et l'arrête.
     */
    public void reset() {
        stop();
        setText("0m 00s");
        this.startTime = null;
    }

    /**
     * Indique si le timer est en cours d'exécution.
     *
     * @return true si le timer est en cours d'exécution
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Retourne le temps écoulé depuis le démarrage.
     *
     * @return java.time.Duration représentant le temps écoulé
     */
    public java.time.Duration getElapsedTime() {
        if (startTime == null) {
            return java.time.Duration.ZERO;
        }
        return java.time.Duration.between(startTime, Instant.now());
    }

    /**
     * Retourne l'instant de démarrage.
     *
     * @return l'instant de démarrage ou null si non démarré
     */
    public Instant getStartTime() {
        return startTime;
    }
}