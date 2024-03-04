package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.util.DeploymentUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
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
    @Builder.Default
    private String name = DeploymentUtil.createRandomName("node-");
    @Builder.Default
    private String type = "edge";
    private String sku;
    @Builder.Default
    private String region = "eastus";

    public static CreateNewAzureNodeDTO fromAbstractNodeDTO(CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        CreateNewAzureNodeDTO createNewAzureNodeDTO = (CreateNewAzureNodeDTO) createNewAbstractNodeDTO;
        return CreateNewAzureNodeDTO.builder()
                .nodeType(createNewAbstractNodeDTO.getNodeType())
                .providerName(createNewAzureNodeDTO.getProviderName())
                .name(createNewAzureNodeDTO.getName())
                .type(createNewAzureNodeDTO.getType())
                .sku(createNewAzureNodeDTO.getSku())
                .region(createNewAzureNodeDTO.getRegion())
                .build();
    }
}
