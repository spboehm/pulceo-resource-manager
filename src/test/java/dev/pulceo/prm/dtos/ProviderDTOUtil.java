package dev.pulceo.prm.dtos;

import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.model.provider.ProviderType;

public class ProviderDTOUtil {
    public static CreateNewAzureProviderDTO  createNewAzureProviderDTO() {
        return CreateNewAzureProviderDTO.builder()
                .providerName("azure-test")
                .providerType(ProviderType.AZURE)
                .clientId("clientId")
                .clientSecret("clientSecret")
                .tenantId("tenantId")
                .subscriptionId("subscriptionId")
                .build();
    }
}
