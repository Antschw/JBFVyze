package fr.antschw.bfv.ui.control;

import fr.antschw.bfv.infrastructure.winnat.WindowUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
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

    // Couleurs pour le thème clair (AtlantaFX PrimerLight)
    // Format 0xAARRGGBB - Alpha(FF), Rouge, Vert, Bleu
    private static final int LIGHT_TITLEBAR_COLOR = 0xFFF6F8FA; // Fond PrimerLight
    private static final int LIGHT_TEXT_COLOR = 0xFF24292F;     // Texte PrimerLight

    // Couleurs pour le thème sombre (AtlantaFX Dracula)
    private static final int DARK_TITLEBAR_COLOR = 0xFF282A36;  // Fond Dracula
    private static final int DARK_TEXT_COLOR = 0xFFF8F8F2;      // Texte Dracula

    // La stage principale (référence pour mise à jour du thème)
    private static Stage mainStage;

    // Détection de Windows
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Initialise le contrôleur de thème.
     */
    public static void initialize() {
        try {
            // Charger le mode sombre depuis les préférences
            darkMode = prefs.getBoolean(DARK_MODE_KEY, false);

            // Appliquer le thème initial
            applyTheme();

            LOGGER.info("ThemeController initialisé avec succès");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'initialisation du ThemeController", e);
        }
    }

    /**
     * Enregistre la fenêtre principale pour les mises à jour de thème.
     */
    public static void setMainStage(Stage stage) {
        try {
            if (stage == null) {
                LOGGER.warn("setMainStage appelé avec stage null");
                return;
            }

            mainStage = stage;

            // Appliquer le thème avec un délai pour permettre à la fenêtre d'être complètement initialisée
            Platform.runLater(() -> {
                try {
                    Thread.sleep(300); // Petit délai pour s'assurer que la fenêtre est complètement initialisée
                    if (isWindows) {
                        applyNativeTheme(stage);
                    }
                } catch (Exception e) {
                    LOGGER.error("Erreur lors de l'application du thème natif (delayed)", e);
                }
            });

            LOGGER.info("Stage principale enregistrée dans ThemeController");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'enregistrement de la stage principale", e);
        }
    }

    /**
     * Bascule entre le thème clair et sombre.
     *
     * @return true si le thème est maintenant sombre, false sinon
     */
    public static boolean toggleTheme() {
        try {
            darkMode = !darkMode;

            // Sauvegarder la préférence
            prefs.putBoolean(DARK_MODE_KEY, darkMode);

            // Appliquer le thème
            applyTheme();

            LOGGER.info("Thème basculé vers mode {}", darkMode ? "sombre" : "clair");
            return darkMode;
        } catch (Exception e) {
            LOGGER.error("Erreur lors du basculement de thème", e);
            return darkMode;
        }
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
                LOGGER.info("Thème sombre appliqué");
            } else {
                // Appliquer thème clair
                Application.setUserAgentStylesheet(
                        new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
                LOGGER.info("Thème clair appliqué");
            }

            // Appliquer le thème natif si la fenêtre principale est initialisée
            if (mainStage != null && mainStage.isShowing() && isWindows) {
                Platform.runLater(() -> {
                    try {
                        applyNativeTheme(mainStage);
                    } catch (Exception e) {
                        LOGGER.error("Erreur lors de l'application du thème natif (delayed)", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'application du thème", e);
        }
    }

    /**
     * Applique le thème natif à la barre de titre de la fenêtre.
     */
    private static void applyNativeTheme(Stage stage) {
        if (!isWindows || stage == null) {
            return;
        }

        try {
            if (darkMode) {
                // Appliquer le mode sombre à la barre de titre
                WindowUtils.setDarkMode(stage, true);
                WindowUtils.setTitleBarColor(stage, DARK_TITLEBAR_COLOR);
                WindowUtils.setTitleTextColor(stage, DARK_TEXT_COLOR);
            } else {
                // Appliquer le mode clair à la barre de titre
                WindowUtils.setDarkMode(stage, false);
                WindowUtils.setTitleBarColor(stage, LIGHT_TITLEBAR_COLOR);
                WindowUtils.setTitleTextColor(stage, LIGHT_TEXT_COLOR);
            }

            LOGGER.info("Thème natif appliqué avec succès");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'application du thème natif", e);
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