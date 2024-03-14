package dev.pulceo.prm.model.node;


import dev.pulceo.prm.dto.node.NodeTagDTO;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class NodeTag extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "node_id")
    private Node node;
    @Column(name = "tag_key")
    private String key;
    @Column(name = "tag_value")
    private String value;

    public static NodeTag fromNodeTagDTO(NodeTagDTO nodeTagDTO) {
        return NodeTag.builder()
                .key(nodeTagDTO.getKey())
                .value(nodeTagDTO.getValue())
                .build();
    }
}
