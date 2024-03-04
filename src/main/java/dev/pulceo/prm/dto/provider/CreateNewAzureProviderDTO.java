package dev.pulceo.prm.dto.provider;

import dev.pulceo.prm.model.provider.ProviderType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Provider")
public class CreateNewAzureProviderDTO {

    @Schema(description = "The name of the provider", example = "azure-provider")
    private String providerName;
    @Schema(description = "The type of the provider", example = "AZURE")
    private ProviderType providerType;
    @Schema(description = "The client ID of the provider", example = "5a19038d-7b24-43cd-adf0-b6cebc83f74f")
    private String clientId;
    @Schema(description = "The client secret of the provider", example = "e616b7e2-1d26-4f34-b9c2-0cf15e9989b0")
    private String clientSecret;
    @Schema(description = "The tenant ID of the provider", example = "abssk~bGasZ9sEzdNbuzaBoMXIX55hivbMl8wEa8s")
    private String tenantId;
    @Schema(description = "The subscription ID of the provider", example = "e616b7e2-1d26-4f34-b9c2-0cf15e9989b0")
    private String subscriptionId;

}
