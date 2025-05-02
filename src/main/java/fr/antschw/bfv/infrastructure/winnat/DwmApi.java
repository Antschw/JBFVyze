package fr.antschw.bfv.infrastructure.winnat;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface JNA pour l'API Desktop Window Manager (DWM) de Windows.
 * Permet de personnaliser l'apparence native de la fenêtre Windows.
 */
public interface DwmApi extends Library {
    Logger LOGGER = LoggerFactory.getLogger(DwmApi.class);

    // Détection de Windows
    boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    // Instance non-statique pour éviter le chargement sur des systèmes non-Windows
    DwmApi INSTANCE = IS_WINDOWS ? initializeInstance() : null;

    private static DwmApi initializeInstance() {
        try {
            return Native.load("dwmapi", DwmApi.class);
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            LOGGER.error("Impossible de charger dwmapi.dll: {}", e.getMessage());
            return null;
        }
    }

    // Constantes pour les attributs DWM
    int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;     // Windows 10 2004 et plus
    int DWMWA_CAPTION_COLOR = 35;               // Couleur de la barre de titre
    int DWMWA_TEXT_COLOR = 36;                  // Couleur du texte de la barre de titre
    int DWMWA_BORDER_COLOR = 34;                // Couleur de la bordure de la fenêtre
    int DWMWA_WINDOW_CORNER_PREFERENCE = 33;    // Préférence pour les coins de la fenêtre

    // Constantes pour les préférences de coins
    int DWMWCP_DEFAULT = 0;      // Utiliser la valeur par défaut du système
    int DWMWCP_DONOTROUND = 1;   // Ne pas arrondir les coins
    int DWMWCP_ROUND = 2;        // Arrondir les coins
    int DWMWCP_ROUNDSMALL = 3;   // Légèrement arrondir les coins

    /**
     * Modifie un attribut de la fenêtre via l'API DWM.
     *
     * @param hwnd La handle de la fenêtre
     * @param dwAttribute L'attribut à modifier
     * @param pvAttribute Pointeur vers la valeur à définir
     * @param cbAttribute Taille de la valeur
     * @return Code d'erreur (0 si succès)
     */
    int DwmSetWindowAttribute(
            WinDef.HWND hwnd,
            int dwAttribute,
            Object pvAttribute,
            int cbAttribute);
}