package dev.pulceo.prm.dto.node;


import dev.pulceo.prm.model.node.NodeTag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class NodeTagDTO {

    private String key;
    private String value;

    public static NodeTagDTO fromNodeTag(NodeTag nodeTag) {
        return NodeTagDTO.builder()
                .key(nodeTag.getKey())
                .value(nodeTag.getValue())
                .build();
    }
}
