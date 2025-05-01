package fr.antschw.bfv.infrastructure.hotkey;

import com.google.inject.Inject;
import fr.antschw.bfv.domain.model.HotkeyConfiguration;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du service de configuration des raccourcis
 * qui utilise SettingsService pour la persistance.
 */
public class HotkeyConfigurationAdapter implements HotkeyConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotkeyConfigurationAdapter.class);
    private final SettingsService settingsService;
    private final HotkeyConfiguration configuration;
    private Runnable onUpdate;

    /**
     * Constructeur avec injection du service de configuration.
     *
     * @param settingsService service de gestion des paramètres
     */
    @Inject
    public HotkeyConfigurationAdapter(SettingsService settingsService) {
        this.settingsService = settingsService;
        // Charger la touche depuis les paramètres sauvegardés
        String savedHotkey = settingsService.getHotkey();
        this.configuration = new HotkeyConfiguration(savedHotkey);
        LOGGER.info("HotkeyConfigurationAdapter initialized with hotkey: {}", savedHotkey);
    }

    @Override
    public HotkeyConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void updateConfiguration(String newHotkey) {
        configuration.setHotkey(newHotkey);
        // Sauvegarder dans les paramètres persistants
        settingsService.setHotkey(newHotkey);

        if (onUpdate != null) {
            onUpdate.run();
        }

        LOGGER.info("Hotkey updated to: {}", newHotkey);
    }

    @Override
    public void setOnHotkeyUpdated(Runnable callback) {
        this.onUpdate = callback;
    }
}