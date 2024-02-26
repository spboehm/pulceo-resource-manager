package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.AzureNode;
import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeMetaData;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NodeDTO extends AbstractNodeDTO {

    private UUID uuid;
    private String providerName;
    private String hostname;
    private UUID pnaUUID;
    private NodePropertiesDTO node;

    public static NodeDTO fromOnPremNode(OnPremNode OnPremNode) {
        OnPremProvider onPremProvider = OnPremNode.getOnPremProvider();
        ProviderMetaData providerMetaData = onPremProvider.getProviderMetaData();
        NodeMetaData nodeMetaData = OnPremNode.getNodeMetaData();
        Node node = OnPremNode.getNode();
        return NodeDTO.builder()
                .uuid(OnPremNode.getUuid())
                .providerName(providerMetaData.getProviderName())
                .hostname(nodeMetaData.getHostname())
                .pnaUUID(nodeMetaData.getPnaUUID())
                .node(NodePropertiesDTO.fromNode(node))
                .build();
    }

    public static NodeDTO fromAzureNode(AzureNode azureNode) {
        AzureProvider azureProvider = azureNode.getAzureProvider();
        ProviderMetaData providerMetaData = azureProvider.getProviderMetaData();
        NodeMetaData nodeMetaData = azureNode.getNodeMetaData();
        Node node = azureNode.getNode();
        return NodeDTO.builder()
                .uuid(azureNode.getUuid())
                .providerName(providerMetaData.getProviderName())
                .hostname(nodeMetaData.getHostname())
                .pnaUUID(nodeMetaData.getPnaUUID())
                .node(NodePropertiesDTO.fromNode(node))
                .build();
    }

}
