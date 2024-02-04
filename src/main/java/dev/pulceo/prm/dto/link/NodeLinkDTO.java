package dev.pulceo.prm.dto.link;

import dev.pulceo.prm.model.link.NodeLink;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NodeLinkDTO extends AbstractLinkDTO {
    private String linkUUID;
    private UUID remoteNodeLinkUUID;
    private String name;
    private UUID srcNodeUUID;
    private UUID destNodeUUID;

    public static NodeLinkDTO fromNodeLink(NodeLink nodeLink) {
        return NodeLinkDTO.builder()
                .linkType(LinkTypeDTO.NODE_LINK)
                .linkUUID(String.valueOf(nodeLink.getUuid()))
                .remoteNodeLinkUUID(nodeLink.getRemoteNodeLinkUUID())
                .name(nodeLink.getName())
                .srcNodeUUID(nodeLink.getSrcNode().getUuid())
                .destNodeUUID(nodeLink.getDestNode().getUuid())
                .build();
    }
}
