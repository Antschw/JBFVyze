package fr.antschw.bfv.infrastructure.config;

import com.google.inject.AbstractModule;
import fr.antschw.bfv.adapters.input.ui.MainController;
import fr.antschw.bfv.adapters.input.ui.ServerView;
import fr.antschw.bfv.adapters.input.ui.SettingsView;
import fr.antschw.bfv.adapters.input.ui.StatsView;
import fr.antschw.bfv.application.service.PlayerStatsFilter;
import fr.antschw.bfv.application.service.ServerScanService;
import fr.antschw.bfvocr.api.BFVOcrFactory;
import fr.antschw.bfvocr.api.BFVOcrService;

/**
 * Guice module for UI-level components and pipeline orchestration.
 */
public class ServerScanUiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainController.class);
        bind(ServerView.class);
        bind(StatsView.class);
        bind(SettingsView.class);
        bind(ServerScanService.class);
        bind(PlayerStatsFilter.class);

        bind(BFVOcrService.class).toInstance(BFVOcrFactory.createDefaultService());
    }
}
