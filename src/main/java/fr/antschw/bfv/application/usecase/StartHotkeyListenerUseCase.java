package fr.antschw.bfv.application.usecase;

import fr.antschw.bfv.domain.service.HotkeyListenerException;
import fr.antschw.bfv.domain.service.HotkeyListenerService;

/**
 * Use case to start listening to hotkey events.
 */
public class StartHotkeyListenerUseCase {

    private final HotkeyListenerService listenerService;

    public StartHotkeyListenerUseCase(HotkeyListenerService listenerService) {
        this.listenerService = listenerService;
    }

    public void startListening(Runnable callback) throws HotkeyListenerException {
        listenerService.startListening(callback);
    }

    public void stopListening() {
        listenerService.stopListening();
    }
}
