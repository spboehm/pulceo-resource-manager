package dev.pulceo.prm.model.node;


import dev.pulceo.prm.dto.node.CreateTagDTO;
import dev.pulceo.prm.dto.node.NodeTagDTO;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class NodeTag extends BaseEntity {

    @ManyToOne(targetEntity = AbstractNode.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "abstract_node_id")
    private AbstractNode abstractNode;
    @ManyToOne(targetEntity = Node.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "node_id")
    private Node node;
    @Column(name = "tagkey")
    private String key;
    @Column(name = "tagvalue")
    private String value;

    public static NodeTag fromNodeTagDTO(NodeTagDTO nodeTagDTO) {
        return NodeTag.builder()
                .key(nodeTagDTO.getKey())
                .value(nodeTagDTO.getValue())
                .build();
    }

    public static NodeTag fromCreateNodeTagDTO(CreateTagDTO createTagDTO, AbstractNode abstractNode, Node node) {
        return NodeTag.builder()
                .abstractNode(abstractNode)
                .node(node)
                .key(createTagDTO.getTagKey())
                .value(createTagDTO.getTagValue())
                .build();
    }

    @Override
    public String toString() {
        return "NodeTag{" +
                "node=" + node.hashCode() +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                "} " + super.toString();
    }
}
