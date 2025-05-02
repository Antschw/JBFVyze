package fr.antschw.bfv.infrastructure.winnat;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitaire pour personnaliser l'apparence native des fenêtres JavaFX.
 * Adapté de BeranaUI pour JavaFX.
 */
public class WindowUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowUtils.class);
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Extension de User32 pour accéder aux fonctions supplémentaires nécessaires
     */
    private interface User32Ex extends User32 {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        long GetWindowLongA(HWND hWnd, int nIndex);
        long SetWindowLongA(HWND hWnd, int nIndex, long dwNewLong);

        // Style constants
        int GWL_STYLE = -16;
        int GWL_EXSTYLE = -20;

        // Window styles
        long WS_CAPTION = 0x00C00000L;
        long WS_THICKFRAME = 0x00040000L;
        long WS_MINIMIZEBOX = 0x00020000L;
        long WS_MAXIMIZEBOX = 0x00010000L;
        long WS_SYSMENU = 0x00080000L;

        // Window extended styles
        long WS_EX_DLGMODALFRAME = 0x00000001L;
        long WS_EX_CLIENTEDGE = 0x00000200L;
        long WS_EX_STATICEDGE = 0x00020000L;
    }

    /**
     * Récupère le handle natif d'une fenêtre JavaFX
     *
     * @param stage La fenêtre JavaFX
     * @return Le handle natif ou null si erreur
     */
    public static WinDef.HWND getNativeWindowHandle(Stage stage) {
        if (!isWindows) {
            LOGGER.warn("Tentative d'accès à l'API Windows sur un OS non-Windows");
            return null;
        }

        try {
            // JavaFX utilise une structure native pour ses fenêtres
            // On doit utiliser une technique spéciale pour obtenir le HWND
            long hwndVal = getWindowHandleFromStage(stage);

            if (hwndVal == 0) {
                LOGGER.error("Handle de fenêtre non trouvé pour la Stage");
                return null;
            }

            HWND hwnd = new HWND(Pointer.createConstant(hwndVal));
            LOGGER.debug("Handle natif trouvé avec succès");
            return hwnd;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération du handle natif de la fenêtre", e);
            return null;
        }
    }

    /**
     * Récupère le handle de fenêtre à partir d'une scène JavaFX.
     * Cette méthode utilise un workaround pour éviter les problèmes de réflexion.
     */
    private static long getWindowHandleFromStage(Stage stage) {
        // Mémoriser le titre actuel
        String oldTitle = stage.getTitle();

        // Générer un titre unique temporaire
        String uniqueTitle = "BFVyze_" + System.nanoTime();
        stage.setTitle(uniqueTitle);

        // Donner le temps au système pour mettre à jour le titre
        try {
            // S'assurer que le titre est appliqué
            Platform.requestNextPulse();
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Chercher la fenêtre par son titre unique
        char[] windowName = new char[uniqueTitle.length() + 1];
        uniqueTitle.getChars(0, uniqueTitle.length(), windowName, 0);
        windowName[uniqueTitle.length()] = '\0';

        HWND hwnd = User32.INSTANCE.FindWindow(null, new String(windowName).trim());

        // Restaurer le titre original
        stage.setTitle(oldTitle);

        return Pointer.nativeValue(hwnd.getPointer());
    }

    /**
     * Active ou désactive le mode sombre pour la barre de titre d'une fenêtre.
     *
     * @param stage La fenêtre JavaFX
     * @param darkMode true pour activer le mode sombre, false pour désactiver
     * @return true si l'opération a réussi
     */
    public static boolean setDarkMode(Stage stage, boolean darkMode) {
        if (!isWindows) {
            LOGGER.debug("Fonction setDarkMode ignorée sur OS non-Windows");
            return false;
        }

        try {
            WinDef.HWND hwnd = getNativeWindowHandle(stage);
            if (hwnd == null) {
                return false;
            }

            // Créer une valeur pour le mode sombre (1 pour activer, 0 pour désactiver)
            int darkModeValue = darkMode ? 1 : 0;

            // Appeler DwmSetWindowAttribute pour modifier l'attribut
            int result = DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DwmApi.DWMWA_USE_IMMERSIVE_DARK_MODE,
                    new int[] { darkModeValue },
                    4  // Taille d'un int
            );

            boolean success = (result == 0);
            if (!success) {
                LOGGER.error("Échec de l'activation du mode sombre: {}", result);
            }

            return success;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'application du mode sombre", e);
            return false;
        }
    }

    /**
     * Définit la couleur de la barre de titre.
     *
     * @param stage La fenêtre JavaFX
     * @param color Le code couleur RGBA (format 0xAARRGGBB)
     * @return true si l'opération a réussi
     */
    public static boolean setTitleBarColor(Stage stage, int color) {
        if (!isWindows) {
            LOGGER.debug("Fonction setTitleBarColor ignorée sur OS non-Windows");
            return false;
        }

        try {
            WinDef.HWND hwnd = getNativeWindowHandle(stage);
            if (hwnd == null) {
                return false;
            }

            int result = DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DwmApi.DWMWA_CAPTION_COLOR,
                    new int[] { color },
                    4  // Taille d'un int
            );

            boolean success = (result == 0);
            if (!success) {
                LOGGER.error("Échec de la définition de la couleur de la barre de titre: {}", result);
            }

            return success;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'application de la couleur de barre de titre", e);
            return false;
        }
    }

    /**
     * Définit la couleur du texte de la barre de titre.
     *
     * @param stage La fenêtre JavaFX
     * @param color Le code couleur RGBA (format 0xAARRGGBB)
     * @return true si l'opération a réussi
     */
    public static boolean setTitleTextColor(Stage stage, int color) {
        if (!isWindows) {
            LOGGER.debug("Fonction setTitleTextColor ignorée sur OS non-Windows");
            return false;
        }

        try {
            WinDef.HWND hwnd = getNativeWindowHandle(stage);
            if (hwnd == null) {
                return false;
            }

            int result = DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DwmApi.DWMWA_TEXT_COLOR,
                    new int[] { color },
                    4  // Taille d'un int
            );

            boolean success = (result == 0);
            if (!success) {
                LOGGER.error("Échec de la définition de la couleur du texte: {}", result);
            }

            return success;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'application de la couleur du texte", e);
            return false;
        }
    }

    /**
     * Personnalise les styles de fenêtre native (comme enlever les bordures ou la barre de titre)
     *
     * @param stage La fenêtre JavaFX
     * @param showTitleBar true pour afficher la barre de titre, false pour la masquer
     * @return true si l'opération a réussi
     */
    public static boolean setWindowStyle(Stage stage, boolean showTitleBar) {
        if (!isWindows) {
            LOGGER.debug("Fonction setWindowStyle ignorée sur OS non-Windows");
            return false;
        }

        try {
            WinDef.HWND hwnd = getNativeWindowHandle(stage);
            if (hwnd == null) {
                return false;
            }

            // Obtenir le style actuel
            long style = User32Ex.INSTANCE.GetWindowLongA(hwnd, User32Ex.GWL_STYLE);

            if (!showTitleBar) {
                // Supprimer les styles de la barre de titre
                style &= ~User32Ex.WS_CAPTION;
                style &= ~User32Ex.WS_SYSMENU;
            } else {
                // Ajouter les styles de la barre de titre
                style |= User32Ex.WS_CAPTION;
                style |= User32Ex.WS_SYSMENU;
            }

            // Appliquer le nouveau style
            User32Ex.INSTANCE.SetWindowLongA(hwnd, User32Ex.GWL_STYLE, style);

            // Demander au système de redessiner la fenêtre
            User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                    User32.SWP_FRAMECHANGED | User32.SWP_NOMOVE |
                            User32.SWP_NOSIZE | User32.SWP_NOZORDER);

            return true;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la modification du style de la fenêtre", e);
            return false;
        }
    }
}