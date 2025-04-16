package fr.antschw.bfv.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fr.antschw.bfv.application.orchestrator.ScanServerOrchestrator;
import fr.antschw.bfv.domain.service.*;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsApiClient;
import fr.antschw.bfv.infrastructure.api.model.ApiType;
import fr.antschw.bfv.infrastructure.screenshot.AwtRobotScreenshotAdapter;

/**
 * Guice module for ScanServerOrchestrator bindings.
 */
public class ScanModule extends AbstractModule {

    @Override
    protected void configure() {
        // Screenshot
        bind(ScreenshotService.class).to(AwtRobotScreenshotAdapter.class);

        // API Clients
        bind(ApiClient.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS.getName()))
                .to(GameToolsApiClient.class);

        bind(ApiClient.class)
                .annotatedWith(Names.named(ApiType.BFVHACKERS.getName()))
                .to(BfvHackersApiClient.class);

        // Orchestrator
        bind(ScanServerOrchestrator.class);
    }
}