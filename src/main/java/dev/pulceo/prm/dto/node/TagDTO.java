package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.NodeTag;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TagDTO {

    private String tagId;
    private TagType tagType;
    private String resourceId;
    private String tagKey;
    private String tagValue;

    public static TagDTO fromNodeTag(NodeTag nodeTag) {
        return TagDTO.builder()
                .tagId(nodeTag.getUuid().toString())
                .tagType(TagType.NODE)
                .resourceId(nodeTag.getNode().getUuid().toString())
                .tagKey(nodeTag.getKey())
                .tagValue(nodeTag.getValue())
                .build();
    }

}
