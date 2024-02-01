package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeMetaData;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OnPremNodeDTO extends AbstractNodeDTO {

    private UUID uuid;
    private String providerName;
    private String hostname;
    private UUID pnaUUID;
    private NodeDTO node;

    public static OnPremNodeDTO fromOnPremNode(OnPremNode OnPremNode) {
            OnPremProvider onPremProvider = OnPremNode.getOnPremProvider();
            ProviderMetaData providerMetaData = onPremProvider.getProviderMetaData();
            NodeMetaData nodeMetaData = OnPremNode.getNodeMetaData();
            Node node = OnPremNode.getNode();
            return OnPremNodeDTO.builder()
                    .uuid(OnPremNode.getUuid())
                    .providerName(providerMetaData.getProviderName())
                    .hostname(nodeMetaData.getHostname())
                    .pnaUUID(nodeMetaData.getPnaUUID())
                    .node(NodeDTO.fromNode(node))
                    .build();
    }

}
