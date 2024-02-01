package dev.pulceo.prm.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
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

    private String providerName;
    private String hostname;
    private String pnaInitToken;

    public static CreateNewOnPremNodeDTO fromAbstractNodeDTO(CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        CreateNewOnPremNodeDTO createNewOnPremNodeDTO = (CreateNewOnPremNodeDTO) createNewAbstractNodeDTO;
        return CreateNewOnPremNodeDTO.builder()
                .nodeType(createNewOnPremNodeDTO.getNodeType())
                .providerName(createNewOnPremNodeDTO.getProviderName())
                .hostname(createNewOnPremNodeDTO.getHostname())
                .pnaInitToken(createNewOnPremNodeDTO.getPnaInitToken())
                .build();

    }

}
