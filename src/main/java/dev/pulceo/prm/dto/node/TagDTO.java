package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.dto.resource.ResourceInformationDTO;
import dev.pulceo.prm.model.node.NodeTag;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagDTO {

    private String tagId;
    private TagType tagType;
    private String tagKey;
    private String tagValue;
    private ResourceInformationDTO parentResource;

    public static TagDTO fromNodeTag(NodeTag nodeTag, String lbEndpoint, String apiBasePath) {
        return TagDTO.builder()
                .tagId(nodeTag.getUuid().toString())
                .tagType(TagType.NODE)
                .parentResource(ResourceInformationDTO.builder()
                        .resourceType("NODE")
                        .resourceUUID(nodeTag.getAbstractNode().getUuid().toString())
                        .resourceId(nodeTag.getAbstractNode().getName())
                        .url(lbEndpoint + apiBasePath + "/" + nodeTag.getAbstractNode().getUuid().toString())
                        .build())
                .tagKey(nodeTag.getKey())
                .tagValue(nodeTag.getValue())
                .build();
    }

}
