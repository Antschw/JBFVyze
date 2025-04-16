package fr.antschw.bfv.infrastructure.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.infrastructure.hotkey.InMemoryHotkeyConfigurationAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HotkeyModuleTest {

    @Test
    void testBindings() {
        Injector injector = Guice.createInjector(new HotkeyModule());

        HotkeyConfigurationService configService = injector.getInstance(HotkeyConfigurationService.class);
        HotkeyListenerService listenerService = injector.getInstance(HotkeyListenerService.class);

        assertInstanceOf(InMemoryHotkeyConfigurationAdapter.class, configService);
        assertNotNull(listenerService);
    }
}
