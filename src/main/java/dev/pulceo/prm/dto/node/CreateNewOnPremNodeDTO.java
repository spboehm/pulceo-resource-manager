package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.util.DeploymentUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "OnPremNode", description = "Create a new node running on an on-premises node.")
public class CreateNewOnPremNodeDTO extends CreateNewAbstractNodeDTO {

    @Builder.Default
    private String name = DeploymentUtil.createRandomName("node-");
    private String providerName;
    private String hostname;
    private String pnaInitToken;

    public static CreateNewOnPremNodeDTO fromAbstractNodeDTO(CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        CreateNewOnPremNodeDTO createNewOnPremNodeDTO = (CreateNewOnPremNodeDTO) createNewAbstractNodeDTO;
        return CreateNewOnPremNodeDTO.builder()
                .name(createNewOnPremNodeDTO.getName())
                .nodeType(createNewOnPremNodeDTO.getNodeType())
                .providerName(createNewOnPremNodeDTO.getProviderName())
                .hostname(createNewOnPremNodeDTO.getHostname())
                .pnaInitToken(createNewOnPremNodeDTO.getPnaInitToken())
                .build();

    }

}
