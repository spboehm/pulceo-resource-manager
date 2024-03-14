package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.util.DeploymentUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private int cpu = 2;
    @Builder.Default
    private int memory = 4;
    @Builder.Default
    private String region = "eastus";
    @Builder.Default
    private List<NodeTagDTO> tags = new ArrayList<>();


    public static CreateNewAzureNodeDTO fromAbstractNodeDTO(CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        CreateNewAzureNodeDTO createNewAzureNodeDTO = (CreateNewAzureNodeDTO) createNewAbstractNodeDTO;
        return CreateNewAzureNodeDTO.builder()
                .nodeType(createNewAbstractNodeDTO.getNodeType())
                .providerName(createNewAzureNodeDTO.getProviderName())
                .name(createNewAzureNodeDTO.getName())
                .type(createNewAzureNodeDTO.getType())
                .cpu(createNewAzureNodeDTO.getCpu())
                .memory(createNewAzureNodeDTO.getMemory())
                .region(createNewAzureNodeDTO.getRegion())
                .tags(createNewAzureNodeDTO.getTags())
                .build();
    }
}
