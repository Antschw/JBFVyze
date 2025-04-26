package fr.antschw.bfv.infrastructure.binding;

import fr.antschw.bfv.domain.service.UserStatsCacheService;
import fr.antschw.bfv.application.orchestrator.PlayerStatsFilter;
import fr.antschw.bfv.application.orchestrator.ServerScanCoordinator;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.domain.service.ScreenshotService;
import fr.antschw.bfv.domain.service.ServerInfoService;
import fr.antschw.bfv.domain.service.ServerPlayersService;
import fr.antschw.bfv.domain.service.UserStatsService;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsClient;
import fr.antschw.bfv.infrastructure.api.client.PlayerClient;
import fr.antschw.bfv.infrastructure.api.type.ApiType;
import fr.antschw.bfv.infrastructure.cache.UserStatsCacheAdapter;
import fr.antschw.bfv.infrastructure.hotkey.HotkeyConfigurationAdapter;
import fr.antschw.bfv.infrastructure.hotkey.HotkeyListenerAdapter;
import fr.antschw.bfv.infrastructure.screenshot.ScreenshotAdapter;
import fr.antschw.bfv.ui.MainController;
import fr.antschw.bfv.ui.view.ServerView;
import fr.antschw.bfv.ui.view.SettingsView;
import fr.antschw.bfv.ui.view.StatsView;
import fr.antschw.bfvocr.api.BFVOcrFactory;
import fr.antschw.bfvocr.api.BFVOcrService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Screenshot adapter
        bind(ScreenshotService.class)
                .to(ScreenshotAdapter.class)
                .in(Singleton.class);

        // GameTools server‐info client
        bind(ServerInfoService.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS.getName()))
                .to(GameToolsClient.class)
                .in(Singleton.class);

        // BFVHackers server‐info client
        bind(ServerInfoService.class)
                .annotatedWith(Names.named(ApiType.BFVHACKERS.getName()))
                .to(BfvHackersClient.class)
                .in(Singleton.class);

        // GameTools players‐list client
        bind(ServerPlayersService.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS_PLAYERS.getName()))
                .to(PlayerClient.class)
                .in(Singleton.class);

        // GameTools individual‐stats client
        bind(UserStatsService.class)
                .annotatedWith(Names.named(ApiType.GAMETOOLS_PLAYERS.getName()))
                .to(PlayerClient.class)
                .in(Singleton.class);

        // Cache for UserStats
        bind(UserStatsCacheService.class)
                .to(UserStatsCacheAdapter.class)
                .asEagerSingleton();

        // Hotkey
        bind(HotkeyConfigurationService.class).to(HotkeyConfigurationAdapter.class).asEagerSingleton();
        bind(HotkeyListenerService.class).to(HotkeyListenerAdapter.class).asEagerSingleton();

        // UI & orchestration
        bind(MainController.class);
        bind(ServerView.class);
        bind(StatsView.class);
        bind(SettingsView.class);
        bind(ServerScanCoordinator.class);
        bind(PlayerStatsFilter.class);

        bind(BFVOcrService.class).toInstance(BFVOcrFactory.createDefaultService());
    }
}
