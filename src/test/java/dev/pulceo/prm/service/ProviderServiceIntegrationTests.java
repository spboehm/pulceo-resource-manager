package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.*;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.OnPremProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProviderServiceIntegrationTests {

    @Autowired
    ProviderService providerService;
    @Autowired
    OnPremProviderRepository onpremProviderRepository;
    @Autowired
    AzureProviderRepository azureProviderRepository;
    @Autowired
    AbstractNodeRepository abstractNodeRepository;
    @Autowired
    AbstractLinkRepository abstractLinkRepository;

    @BeforeEach
    public void prepare() {
        this.abstractLinkRepository.deleteAll();
        this.abstractNodeRepository.deleteAll();
        this.onpremProviderRepository.deleteAll();
        this.azureProviderRepository.deleteAll();
    }

    @Test
    public void testCreateAzureProvider () {
        // given
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder().providerName("azure-provider").providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().tenantId("s").clientId("s").clientSecret("s").subscriptionId("s").build())
                .build();
        // when
        AzureProvider actualAzureProvider = this.providerService.createAzureProvider(azureProvider);

        // then
        assertEquals(azureProvider, actualAzureProvider);

    }

    @Test
    public void testCreateOnPremProvider () {
        // given
        OnPremProvider onPremProvider = OnPremProvider.builder()
                .providerMetaData(ProviderMetaData.builder().providerName("onprem-provider-test").providerType(ProviderType.ON_PREM).build())
                .build();

        // when
        OnPremProvider actualOnPremProvider = this.providerService.createOnPremProvider(onPremProvider);

        // then
        assertEquals(onPremProvider, actualOnPremProvider);
    }

}
