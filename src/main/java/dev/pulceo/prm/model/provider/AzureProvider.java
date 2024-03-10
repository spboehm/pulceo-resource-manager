package dev.pulceo.prm.model.provider;


import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.model.BaseEntity;
import dev.pulceo.prm.model.node.AzureNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class AzureProvider extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ProviderMetaData providerMetaData;
    @OneToMany(mappedBy = "azureProvider")
    private List<AzureNode> azureNodes;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AzureCredentials credentials;

    public static AzureProvider fromCreateNewAzureProviderDTO(CreateNewAzureProviderDTO createNewAzureProviderDTO) {
        return AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder()
                        .providerName(createNewAzureProviderDTO.getProviderName())
                        .providerType(createNewAzureProviderDTO.getProviderType())
                        .build())
                .credentials(AzureCredentials.builder()
                        .clientId(createNewAzureProviderDTO.getClientId())
                        .clientSecret(createNewAzureProviderDTO.getClientSecret())
                        .tenantId(createNewAzureProviderDTO.getTenantId())
                        .subscriptionId(createNewAzureProviderDTO.getSubscriptionId())
                        .build())
                .build();
    }

    @Override
    public String toString() {
        return "AzureProvider{" +
                "providerMetaData=" + providerMetaData +
                '}';
    }
}
