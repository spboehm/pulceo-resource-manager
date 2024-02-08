package dev.pulceo.prm.dto.pna.node.CPU;


import dev.pulceo.prm.model.node.CPUResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CPUResourceDTO {
    private UUID uuid;
    private UUID nodeUUID;
    private String nodeName;
    private CPUDTO cpuCapacity;
    private CPUDTO cpuAllocatable;

    public static CPUResourceDTO fromCPUResource(UUID nodeUUID, String nodeName, CPUResource cpuResource) {
        return CPUResourceDTO.builder()
                .uuid(cpuResource.getUuid())
                .nodeName(nodeName)
                .nodeUUID(nodeUUID)
                .cpuCapacity(CPUDTO.fromCPU(cpuResource.getCpuCapacity()))
                .cpuAllocatable(CPUDTO.fromCPU(cpuResource.getCpuAllocatable()))
                .build();
    }

}
