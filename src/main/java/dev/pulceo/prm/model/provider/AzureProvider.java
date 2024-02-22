package dev.pulceo.prm.model.provider;


import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AzureProvider extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ProviderMetaData providerMetaData;
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
}
