package dev.pulceo.prm.model.node;

import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
// TODO: remove cpuResource
@NamedEntityGraph(
        name = "graph.OnPremNode.onPremProvider",
        attributeNodes = {
                @NamedAttributeNode(value = "onPremProvider", subgraph = "graph.OnPremNode.onPremProvider"),
                @NamedAttributeNode("nodeMetaData"),
                @NamedAttributeNode(value = "node", subgraph = "graph.OnPremNode.node"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "graph.OnPremNode.onPremProvider",
                        attributeNodes = {
                                @NamedAttributeNode("providerMetaData")
                        }
                ),
                @NamedSubgraph(
                        name = "graph.OnPremNode.node",
                        attributeNodes = {
                                @NamedAttributeNode("cpuResource")
                        }
                ),
        }
)
public class OnPremNode extends AbstractNode {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn
    private OnPremProvider onPremProvider;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NodeMetaData nodeMetaData;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Node node;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private CloudRegistration cloudRegistration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnPremNode that = (OnPremNode) o;

        if (!Objects.equals(onPremProvider, that.onPremProvider))
            return false;
        if (!Objects.equals(nodeMetaData, that.nodeMetaData)) return false;
        if (!Objects.equals(node, that.node)) return false;
        return Objects.equals(cloudRegistration, that.cloudRegistration);
    }

    @Override
    public int hashCode() {
        int result = onPremProvider != null ? onPremProvider.hashCode() : 0;
        result = 31 * result + (nodeMetaData != null ? nodeMetaData.hashCode() : 0);
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (cloudRegistration != null ? cloudRegistration.hashCode() : 0);
        return result;
    }

    @Override
    public G6Node getG6Node() {
        return G6Node.builder()
                .id(String.valueOf(this.getUuid()))
                .name(this.getNode().getName())
                .build();
    }
}
