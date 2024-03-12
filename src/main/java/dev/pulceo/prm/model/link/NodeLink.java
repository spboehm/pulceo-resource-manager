package dev.pulceo.prm.model.link;

import dev.pulceo.prm.internal.G6.model.G6Edge;
import dev.pulceo.prm.model.node.AbstractNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class NodeLink extends AbstractLink {

    // TODO: move to MetaDataClass, refers to the src node
    private UUID remoteNodeLinkUUID;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private AbstractNode srcNode;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private AbstractNode destNode;
    // TODO: List of metric requests
    // TODO: List of metrics

    @Override
    public G6Edge getG6Edge() {
        return G6Edge.builder()
                .id(String.valueOf(this.getUuid()))
                .source(String.valueOf(this.getSrcNode().getUuid()))
                .target(String.valueOf(this.getDestNode().getUuid()))
                .label(this.getName())
                .build();
    }

    @Override
    public String toString() {
        return "NodeLink{" +
                "remoteNodeLinkUUID=" + remoteNodeLinkUUID +
                ", srcNode=" + srcNode +
                ", destNode=" + destNode +
                '}';
    }
}
