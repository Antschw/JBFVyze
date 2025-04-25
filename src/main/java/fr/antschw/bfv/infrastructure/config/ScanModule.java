package fr.antschw.bfv.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import fr.antschw.bfv.application.orchestrator.ScanServerOrchestrator;
import fr.antschw.bfv.application.port.UserStatsCachePort;
import fr.antschw.bfv.domain.service.ServerInfoClient;
import fr.antschw.bfv.domain.service.ServerPlayersClient;
import fr.antschw.bfv.domain.service.UserStatsClient;
import fr.antschw.bfv.domain.service.ScreenshotService;

import fr.antschw.bfv.infrastructure.api.client.BfvHackersApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsPlayerApiClient;

import fr.antschw.bfv.infrastructure.api.model.ApiType;
import fr.antschw.bfv.infrastructure.cache.JsonUserStatsCacheAdapter;
import fr.antschw.bfv.infrastructure.screenshot.AwtRobotScreenshotAdapter;

/**
 * Guice module that binds all services needed for server scanning.
 */
public class ScanModule extends AbstractModule {

    @Override
    protected void configure() {
        // Screenshot adapter
        bind(ScreenshotService.class)
                .to(AwtRobotScreenshotAdapter.class)
                .in(Singleton.class);

        // GameTools server‐info client
        bind(ServerInfoClient.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS.getName()))
                .to(GameToolsApiClient.class)
                .in(Singleton.class);

        // BFVHackers server‐info client
        bind(ServerInfoClient.class)
                .annotatedWith(Names.named(ApiType.BFVHACKERS.getName()))
                .to(BfvHackersApiClient.class)
                .in(Singleton.class);

        // GameTools players‐list client
        bind(ServerPlayersClient.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS_PLAYERS.getName()))
                .to(GameToolsPlayerApiClient.class)
                .in(Singleton.class);

        // GameTools individual‐stats client
        bind(UserStatsClient.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS_PLAYERS.getName()))
                .to(GameToolsPlayerApiClient.class)
                .in(Singleton.class);

        // Cache for UserStats
        bind(UserStatsCachePort.class)
                .to(JsonUserStatsCacheAdapter.class)
                .asEagerSingleton();

        // Orchestrator that drives the scan flow
        bind(ScanServerOrchestrator.class)
                .in(Singleton.class);
    }
}
