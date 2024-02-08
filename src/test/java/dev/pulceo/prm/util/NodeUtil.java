package dev.pulceo.prm.util;

import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.model.registration.CloudRegistration;

import java.util.UUID;

public class NodeUtil {

    public static OnPremNode createTestOnPremNode(UUID remoteUUID, UUID pnaUUID, String hostName) {
        OnPremProvider onPremProvider = OnPremProvider.builder().providerMetaData(
                ProviderMetaData.builder()
                        .providerName("default")
                        .providerType(ProviderType.ON_PREM)
                        .build()).build();

        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .remoteNodeUUID(remoteUUID)
                .pnaUUID(pnaUUID)
                .hostname(hostName)
                .build();

        CPU cpuAllocatable = CPU.builder()
                .modelName("12th Gen Intel(R) Core(TM) i7-1260P")
                .cores(12)
                .threads(24)
                .bogoMIPS(4993.00f)
                .MIPS(4993.00f)
                .GFlop(0.0f)
                .minimalFrequency(400.0000f)
                .maximalFrequency(4700.0000f)
                .averageFrequency(2550.0000f)
                .slots(0.0f)
                .shares(24000)
                .build();

        CPU cpuCapacity = CPU.builder()
                .modelName("12th Gen Intel(R) Core(TM) i7-1260P")
                .cores(12)
                .threads(24)
                .bogoMIPS(4993.00f)
                .MIPS(4993.00f)
                .GFlop(0.0f)
                .minimalFrequency(400.0000f)
                .maximalFrequency(4700.0000f)
                .averageFrequency(2550.0000f)
                .slots(0.0f)
                .shares(24000)
                .build();

        CPUResource cpuResource = CPUResource.builder()
                .cpuAllocatable(cpuAllocatable)
                .cpuCapacity(cpuCapacity)
                .build();

        Node node = Node.builder()
                .name(hostName)
                .cpuResource(cpuResource)
                .build();

        CloudRegistration cloudRegistration = CloudRegistration.builder()
                .nodeUUID(remoteUUID)
                .pnaUUID(pnaUUID)
                .prmUUID(UUID.fromString("ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a"))
                .prmEndpoint("http://localhost:7878")
                .pnaToken("dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl")
                .build();

        return OnPremNode.builder()
                .onPremProvider(onPremProvider)
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();
    }

}
