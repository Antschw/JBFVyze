
package fr.antschw.bfv.infrastructure.api.client;

import fr.antschw.bfv.domain.service.ApiClient;
import fr.antschw.bfv.infrastructure.api.model.ApiType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientFactoryTest {

    @Test
    void testCreateGameToolsApiClient() {
        ApiClient client = ApiClientFactory.createApiClient(ApiType.GAMETOOLS);
        assertInstanceOf(GameToolsApiClient.class, client);
    }

    @Test
    void testCreateBfvHackersApiClient() {
        ApiClient client = ApiClientFactory.createApiClient(ApiType.BFVHACKERS);
        assertInstanceOf(BfvHackersApiClient.class, client);
    }

    @Test
    void testPrivateConstructor() throws Exception {
        var constructor = ApiClientFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
