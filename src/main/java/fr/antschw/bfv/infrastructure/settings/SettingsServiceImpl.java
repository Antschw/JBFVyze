package fr.antschw.bfv.infrastructure.settings;

import fr.antschw.bfv.domain.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Service de gestion des paramètres persistants de l'application.
 * Sauvegarde et charge les préférences dans un fichier properties.
 */
public class SettingsServiceImpl implements SettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsServiceImpl.class);
    private static final Path SETTINGS_FILE = Path.of(System.getProperty("user.home"), ".bfvyze", "settings.properties");

    private final Properties properties = new Properties();

    private static final String KEY_HOTKEY = "hotkey";
    private static final String KEY_PLAYER_NAME = "playerName";
    private static final String KEY_USE_PLAYER_ID = "usePlayerId";

    private static final String DEFAULT_HOTKEY = "F12";

    /**
     * Construit le service et charge les paramètres depuis le fichier.
     */
    public SettingsServiceImpl() {
        loadSettings();
    }

    @Override
    public String getHotkey() {
        return properties.getProperty(KEY_HOTKEY, DEFAULT_HOTKEY);
    }

    @Override
    public void setHotkey(String hotkey) {
        properties.setProperty(KEY_HOTKEY, hotkey);
        saveSettings();
    }

    @Override
    public String getPlayerName() {
        return properties.getProperty(KEY_PLAYER_NAME, "");
    }

    @Override
    public void setPlayerName(String playerName) {
        properties.setProperty(KEY_PLAYER_NAME, playerName);
        saveSettings();
    }

    @Override
    public boolean isUsePlayerId() {
        return Boolean.parseBoolean(properties.getProperty(KEY_USE_PLAYER_ID, "false"));
    }

    @Override
    public void setUsePlayerId(boolean usePlayerId) {
        properties.setProperty(KEY_USE_PLAYER_ID, String.valueOf(usePlayerId));
        saveSettings();
    }

    /**
     * Charge les paramètres depuis le fichier.
     */
    private void loadSettings() {
        File file = SETTINGS_FILE.toFile();
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                LOGGER.info("Settings loaded from {}", SETTINGS_FILE);
            } catch (IOException e) {
                LOGGER.error("Failed to load settings", e);
            }
        } else {
            LOGGER.info("No settings file found, using defaults");
        }
    }

    /**
     * Sauvegarde les paramètres dans le fichier.
     * Ne crée le fichier que si des valeurs non-par défaut sont définies.
     */
    private void saveSettings() {
        try {
            // Vérifier s'il n'y a que des valeurs par défaut, dans ce cas ne pas créer de fichier
            if (isUsingOnlyDefaults()) {
                LOGGER.debug("Using only default settings, no file creation needed");

                // Si un fichier existe déjà, le supprimer car on est revenu aux valeurs par défaut
                File targetFile = SETTINGS_FILE.toFile();
                if (targetFile.exists() && targetFile.delete()) {
                    LOGGER.info("Removed settings file as all values are default");
                }

                return;
            }

            // Sinon, créer le répertoire si nécessaire
            File dir = SETTINGS_FILE.getParent().toFile();
            if (!dir.exists() && !dir.mkdirs()) {
                LOGGER.error("Failed to create settings directory: {}", dir.getAbsolutePath());
                return;
            }

            // Écrire dans un fichier temporaire puis renommer
            File tempFile = new File(dir, "settings.tmp");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                properties.store(fos, "BFVyze settings");
            }

            // Supprimer l'ancien fichier s'il existe
            File targetFile = SETTINGS_FILE.toFile();
            if (targetFile.exists() && !targetFile.delete()) {
                LOGGER.warn("Could not delete existing settings file");
            }

            // Renommer le fichier temporaire
            if (tempFile.renameTo(targetFile)) {
                LOGGER.info("Settings saved to {}", SETTINGS_FILE);
            } else {
                LOGGER.error("Failed to rename temporary settings file");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save settings", e);
        }
    }

    /**
     * Vérifie si seules les valeurs par défaut sont utilisées.
     *
     * @return true si seules les valeurs par défaut sont utilisées
     */
    private boolean isUsingOnlyDefaults() {
        // Vérifier chaque paramètre
        return getHotkey().equals(DEFAULT_HOTKEY) &&
                getPlayerName().isEmpty() &&
                !isUsePlayerId();
    }
}