package dev.pulceo.prm.dto.link;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateNewNodeLinkDTO extends CreateNewAbstractLinkDTO {
    private String name;
    private UUID srcNodeUUID;
    private UUID destNodeUUID;

    public static CreateNewNodeLinkDTO fromAbstractLinkDTO(CreateNewAbstractLinkDTO createNewAbstractLinkDTO) {
        CreateNewNodeLinkDTO createNewNodeLinkDTO = (CreateNewNodeLinkDTO) createNewAbstractLinkDTO;
        return CreateNewNodeLinkDTO.builder()
                .linkType(createNewNodeLinkDTO.getLinkType())
                .name(createNewNodeLinkDTO.getName())
                .srcNodeUUID(createNewNodeLinkDTO.getSrcNodeUUID())
                .destNodeUUID(createNewNodeLinkDTO.getDestNodeUUID())
                .build();
    }
}
