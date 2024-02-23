package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AzureProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AzureDeploymentServiceIntegrationTests {

    @Autowired
    private AzureDeploymentService azureDeploymentService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AzureProviderRepository azureProviderRepository;

    @Value("${AZURE_SUBSCRIPTION_ID}")
    private String subscriptionId;
    @Value("${AZURE_CLIENT_ID}")
    private String clientId;
    @Value("${AZURE_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${AZURE_TENANT_ID}")
    private String tenantId;

    @BeforeEach
    public void prepare() {
        this.azureProviderRepository.deleteAll();
    }

    @Test
    @Disabled
    public void testDeleteAzureVirtualMachine() {
        // given
        String providerName = "azure-provider";
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder().providerName("azure-provider").providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().tenantId(this.tenantId).clientId(this.clientId).clientSecret(this.clientSecret).subscriptionId(this.subscriptionId).build())
                .build();
        AzureProvider actualAzureProvider = this.providerService.createAzureProvider(azureProvider);
        String resourceGroupName = "pulceo-test-09";
        String vmName = "testLinuxVM";

        // when
        this.azureDeploymentService.deleteAzureVirtualMachine(resourceGroupName, providerName, true);

        // then

    }

}
