package fr.antschw.bfv.infrastructure.hotkey;

import fr.antschw.bfv.domain.model.HotkeyConfiguration;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;

/**
 * In-memory implementation of HotkeyConfigurationService.
 */
public class HotkeyConfigurationAdapter implements HotkeyConfigurationService {

    private static final String DEFAULT_HOTKEY = "F12";
    private final HotkeyConfiguration configuration;
    private Runnable onUpdate;

    public HotkeyConfigurationAdapter() {
        this.configuration = new HotkeyConfiguration(DEFAULT_HOTKEY);
    }

    @Override
    public HotkeyConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void updateConfiguration(String newHotkey) {
        configuration.setHotkey(newHotkey);
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    @Override
    public void setOnHotkeyUpdated(Runnable callback) {
        this.onUpdate = callback;
    }
}