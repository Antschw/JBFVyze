package fr.antschw.bfv.ui.control;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Contrôleur pour gérer le thème de l'application et sa persistance.
 */
public class ThemeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeController.class);
    private static final String DARK_MODE_KEY = "darkMode";

    private static boolean darkMode = false;
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeController.class);

    /**
     * Initialise le contrôleur de thème.
     */
    public static void initialize() {
        // Charger le mode sombre depuis les préférences
        darkMode = prefs.getBoolean(DARK_MODE_KEY, false);

        // Appliquer le thème initial
        applyTheme();
    }

    /**
     * Bascule entre le thème clair et sombre.
     *
     * @return true si le thème est maintenant sombre, false sinon
     */
    public static boolean toggleTheme() {
        darkMode = !darkMode;

        // Sauvegarder la préférence
        prefs.putBoolean(DARK_MODE_KEY, darkMode);

        // Appliquer le thème
        applyTheme();

        return darkMode;
    }

    /**
     * Applique le thème actuel à l'application.
     */
    private static void applyTheme() {
        try {
            if (darkMode) {
                // Appliquer thème sombre
                Application.setUserAgentStylesheet(
                        new atlantafx.base.theme.Dracula().getUserAgentStylesheet());
                LOGGER.info("Dark theme applied");
            } else {
                // Appliquer thème clair
                Application.setUserAgentStylesheet(
                        new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
                LOGGER.info("Light theme applied");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to apply theme", e);
        }
    }

    /**
     * Vérifie si le mode sombre est actif.
     *
     * @return true si le mode sombre est actif
     */
    public static boolean isDarkMode() {
        return darkMode;
    }
}