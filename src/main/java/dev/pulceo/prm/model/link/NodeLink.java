package dev.pulceo.prm.model.link;

import dev.pulceo.prm.internal.G6.model.G6Edge;
import dev.pulceo.prm.model.node.AbstractNode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class NodeLink extends AbstractLink {

    private String name;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private AbstractNode srcNode;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
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
}
