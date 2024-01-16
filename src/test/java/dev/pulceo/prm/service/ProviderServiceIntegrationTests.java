package dev.pulceo.prm.service;

import dev.pulceo.prm.exception.ProviderServiceException;
import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.repository.ProviderRepository;
import dev.pulceo.prm.util.ProviderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProviderServiceIntegrationTests {

    @Autowired
    ProviderService providerService;

    @Test
    public void testCreateProvider() throws ProviderServiceException {
        // given
        Provider testProvider = ProviderUtil.createTestProvider();

        // when
        Provider createdProvider = this.providerService.createProvider(testProvider);

        // then
        assertEquals(testProvider, createdProvider);
    }

    @Test
    public void testIfDefaultProviderExists() {
        // given
        Optional<Provider> defaultProvider = this.providerService.readDefaultProvider();

        // then
        assertTrue(defaultProvider.isPresent());
    }

}
