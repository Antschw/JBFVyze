package fr.antschw.bfv.domain.service;

import fr.antschw.bfv.domain.exception.HotkeyListenerException;

/**
 * Defines contract for listening to global hotkey events.
 */
public interface HotkeyListenerService {

    /**
     * Starts listening for the configured hotkey.
     *
     * @param onHotkeyPressed callback to execute when hotkey is triggered
     * @throws HotkeyListenerException if listener setup fails
     */
    void startListening(Runnable onHotkeyPressed) throws HotkeyListenerException;

    /**
     * Stops listening for hotkey events.
     */
    void stopListening();
}
