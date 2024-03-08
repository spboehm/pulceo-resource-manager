package dev.pulceo.prm.dto.link;

import dev.pulceo.prm.util.DeploymentUtil;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateNewNodeLinkDTO extends CreateNewAbstractLinkDTO {
    @Builder.Default
    private String name = DeploymentUtil.createRandomName("link-");
    private String srcNodeId;
    private String destNodeId;

    public static CreateNewNodeLinkDTO fromAbstractLinkDTO(CreateNewAbstractLinkDTO createNewAbstractLinkDTO) {
        CreateNewNodeLinkDTO createNewNodeLinkDTO = (CreateNewNodeLinkDTO) createNewAbstractLinkDTO;
        return CreateNewNodeLinkDTO.builder()
                .name(createNewNodeLinkDTO.getName())
                .srcNodeId(createNewNodeLinkDTO.getSrcNodeId())
                .destNodeId(createNewNodeLinkDTO.getDestNodeId())
                .build();
    }
}
