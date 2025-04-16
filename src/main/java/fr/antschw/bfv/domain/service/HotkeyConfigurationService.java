package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.model.HotkeyConfiguration;

/**
 * Defines contract for managing hotkey configuration.
 */
public interface HotkeyConfigurationService {

    /**
     * Retrieves the current hotkey configuration.
     *
     * @return HotkeyConfiguration
     */
    HotkeyConfiguration getConfiguration();

    /**
     * Updates the hotkey configuration.
     *
     * @param newHotkey the new hotkey string
     */
    void updateConfiguration(String newHotkey);

    void setOnHotkeyUpdated(Runnable callback);
}
