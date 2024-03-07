package dev.pulceo.prm.model.node;

import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(
        name = "graph.AzureNode.azureProvider",
        attributeNodes = {
                @NamedAttributeNode(value = "azureProvider", subgraph = "graph.AzureNode.azureProvider"),
                @NamedAttributeNode("nodeMetaData"),
                @NamedAttributeNode(value = "node", subgraph = "graph.AzureNode.node"),
                @NamedAttributeNode("azureDeloymentResult"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "graph.AzureNode.azureProvider",
                        attributeNodes = {
                                @NamedAttributeNode("providerMetaData")
                        }
                ),
                @NamedSubgraph(
                        name = "graph.AzureNode.node",
                        attributeNodes = {
                                @NamedAttributeNode("cpuResource")
                        }
                ),
        }
)
public class AzureNode extends AbstractNode {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn
    private AzureProvider azureProvider;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NodeMetaData nodeMetaData;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Node node;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private CloudRegistration cloudRegistration;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AzureDeloymentResult azureDeloymentResult;
    // TODO: status

    @Override
    public G6Node getG6Node() {
        return G6Node.builder()
                .id(String.valueOf(this.getUuid()))
                .name(this.getNode().getName())
                .build();
    }

    @Override
    public String getToken() {
        return this.cloudRegistration.getPnaToken();
    }
}
