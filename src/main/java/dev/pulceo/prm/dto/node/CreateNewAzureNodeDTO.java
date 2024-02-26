package dev.pulceo.prm.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AzureNode", description = "Create a new node on Azure.")
@ToString
public class CreateNewAzureNodeDTO extends CreateNewAbstractNodeDTO {
    // nodeType in super class
    private String providerName;
    private String name;
    private String type;
    private String sku;
    private String nodeLocationCountry;
    private String nodeLocationCity;

    public static CreateNewAzureNodeDTO fromAbstractNodeDTO(CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        CreateNewAzureNodeDTO createNewAzureNodeDTO = (CreateNewAzureNodeDTO) createNewAbstractNodeDTO;
        return CreateNewAzureNodeDTO.builder()
                .nodeType(createNewAbstractNodeDTO.getNodeType())
                .providerName(createNewAzureNodeDTO.getProviderName())
                .name(createNewAzureNodeDTO.getName())
                .type(createNewAzureNodeDTO.getType())
                .sku(createNewAzureNodeDTO.getSku())
                .nodeLocationCountry(createNewAzureNodeDTO.getNodeLocationCountry())
                .nodeLocationCity(createNewAzureNodeDTO.getNodeLocationCity())
                .build();
    }
}
