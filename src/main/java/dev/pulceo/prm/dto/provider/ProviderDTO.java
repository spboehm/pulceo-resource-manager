package dev.pulceo.prm.dto.provider;

import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderDTO {

    private UUID uuid;
    private String providerName;
    private ProviderType providerType;

    public static ProviderDTO fromAzureProvider(AzureProvider azureProvider) {
        return ProviderDTO.builder()
                .uuid(azureProvider.getUuid())
                .providerName(azureProvider.getProviderMetaData().getProviderName())
                .providerType(azureProvider.getProviderMetaData().getProviderType())
                .build();
    }

}
