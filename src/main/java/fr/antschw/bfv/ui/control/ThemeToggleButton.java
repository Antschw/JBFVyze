package fr.antschw.bfv.ui.control;

import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bouton qui permet de basculer entre le thème clair et sombre.
 */
public class ThemeToggleButton extends Button {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeToggleButton.class);

    /**
     * Crée un nouveau bouton de bascule de thème.
     */
    public ThemeToggleButton() {
        getStyleClass().add("theme-toggle-button");

        // Initialiser l'icône en fonction du thème actuel
        updateIcon();

        // Action du bouton
        setOnAction(e -> {
            boolean isDarkMode = ThemeController.toggleTheme();
            LOGGER.info("Theme toggled to {}", isDarkMode ? "dark" : "light");
            updateIcon();
        });
    }

    /**
     * Met à jour l'icône du bouton en fonction du thème actuel.
     */
    private void updateIcon() {
        FontIcon icon;
        if (ThemeController.isDarkMode()) {
            // Si on est en mode sombre, montrer le soleil pour revenir en clair
            icon = new FontIcon("mdi2w-weather-sunny");
        } else {
            // Si on est en mode clair, montrer la lune pour passer en sombre
            icon = new FontIcon("mdi2w-weather-night");
        }

        icon.setIconSize(14);
        setGraphic(icon);
    }
}