package dev.pulceo.prm.util;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeMetaData;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.model.registration.CloudRegistration;

import java.util.UUID;

public class NodeUtil {

    public static OnPremNode createTestOnPremNode(UUID pnaUUID, String hostName, UUID prmUUID, String prmEndpoint, String pnaToken) {
        OnPremProvider onPremProvider = OnPremProvider.builder().providerMetaData(
                ProviderMetaData.builder()
                        .providerName("default")
                        .providerType(ProviderType.ON_PREM)
                        .build()).build();

        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .pnaUUID(pnaUUID)
                .hostname(hostName)
                .build();

        Node node = Node.builder()
                .name(hostName)
                .build();

        CloudRegistration cloudRegistration = CloudRegistration.builder()
                .pnaUUID(pnaUUID)
                .prmUUID(prmUUID)
                .prmEndpoint(prmEndpoint)
                .pnaToken(pnaToken)
                .build();

        OnPremNode onPremNode = OnPremNode.builder()
                .onPremProvider(onPremProvider)
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();

        return onPremNode;
    }

}
