package dev.pulceo.prm.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
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

}
