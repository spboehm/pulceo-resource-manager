package dev.pulceo.prm.dto.pna.node;

import dev.pulceo.prm.dto.link.CreateNewAbstractLinkDTO;
import dev.pulceo.prm.util.DeploymentUtil;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateNewNodeLinkOnPNADTO extends CreateNewAbstractLinkDTO {
    @Builder.Default
    private String name = DeploymentUtil.createRandomName("link-");
    private UUID srcNodeUUID;
    private UUID destNodeUUID;

}
