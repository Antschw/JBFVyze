package fr.antschw.bfv.infrastructure.hotkey;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.inject.Inject;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerException;
import fr.antschw.bfv.domain.service.HotkeyListenerService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;


/**
 * Implementation of HotkeyListenerService using JNativeHook.
 */
public class JNativeHookHotkeyListenerAdapter implements HotkeyListenerService, NativeKeyListener {

    private final HotkeyConfigurationService configurationService;
    private Runnable callback;
    private static final Logger LOGGER = LoggerFactory.getLogger(JNativeHookHotkeyListenerAdapter.class);

    @Inject
    public JNativeHookHotkeyListenerAdapter(HotkeyConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void startListening(Runnable onHotkeyPressed) throws HotkeyListenerException {
        try {
            // Deactivate internal logs JNativeHook
            java.util.logging.Logger jnativeLogger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            jnativeLogger.setLevel(Level.OFF);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            this.callback = onHotkeyPressed;
            LOGGER.info("Global hotkey listener started.");
        } catch (Exception e) {
            LOGGER.error("Failed to start global hotkey listener.", e);
            throw new HotkeyListenerException("Failed to start hotkey listener", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopListening() {
        try {
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeKeyListener(this);
            LOGGER.info("Global hotkey listener stopped.");
        } catch (Exception e) {
            LOGGER.warn("Error while stopping hotkey listener.", e);
        } finally {
            this.callback = null;
        }
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());

        String currentHotkey = configurationService.getConfiguration().getHotkey();
        if (keyText.equalsIgnoreCase(currentHotkey)) {
            LOGGER.info("Configured hotkey [{}] pressed.", currentHotkey);
            if (callback != null) {
                callback.run();
            }
        }
    }


    @Override public void nativeKeyReleased(NativeKeyEvent nativeEvent) { }
    @Override public void nativeKeyTyped(NativeKeyEvent nativeEvent) { }
}