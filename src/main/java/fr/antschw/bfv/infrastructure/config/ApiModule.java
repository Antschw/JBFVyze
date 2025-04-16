package fr.antschw.bfv.infrastructure.config;

import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsApiClient;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Guice module for binding API clients.
 */
public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        // Binding GameTools API Client with name "GameTools"
        bind(ApiClient.class)
                .annotatedWith(Names.named("GameTools"))
                .to(GameToolsApiClient.class);

        // Binding BFVHackers API Client with name "BFVHackers"
        bind(ApiClient.class)
                .annotatedWith(Names.named("BFVHackers"))
                .to(BfvHackersApiClient.class);
    }
}
