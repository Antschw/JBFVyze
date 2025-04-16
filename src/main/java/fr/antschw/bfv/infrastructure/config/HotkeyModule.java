package fr.antschw.bfv.infrastructure.config;

import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.infrastructure.hotkey.InMemoryHotkeyConfigurationAdapter;
import fr.antschw.bfv.infrastructure.hotkey.JNativeHookHotkeyListenerAdapter;
import com.google.inject.AbstractModule;

/**
 * Guice module for binding hotkey services.
 */
public class HotkeyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HotkeyConfigurationService.class).to(InMemoryHotkeyConfigurationAdapter.class).asEagerSingleton();
        bind(HotkeyListenerService.class).to(JNativeHookHotkeyListenerAdapter.class).asEagerSingleton();
    }
}
