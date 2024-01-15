package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.util.ProviderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProviderServiceIntegrationTests {

    @Autowired
    ProviderService providerService;

    @Test
    public void testCreateProvider() {
        // given
        Provider testProvider = ProviderUtil.createTestProvider();

        // when
        Provider createdProvider = this.providerService.createProvider(testProvider);

        // then
        assertEquals(testProvider, createdProvider);
    }

}
