package fr.antschw.bfv.application.usecase;

import fr.antschw.bfv.domain.service.HotkeyConfigurationService;

/**
 * Use case to configure the hotkey.
 */
public class ConfigureHotkeyUseCase {

    private final HotkeyConfigurationService configurationService;

    public ConfigureHotkeyUseCase(HotkeyConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void updateHotkey(String newHotkey) {
        configurationService.updateConfiguration(newHotkey);
    }
}
