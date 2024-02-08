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
    private UUID nodeUUID;
    private CPUDTO cpuCapacity;
    private CPUDTO cpuAllocatable;

    public static CPUResourceDTO fromCPUResource(UUID nodeUUID, CPUResource cpuResource) {
        return CPUResourceDTO.builder()
                .nodeUUID(nodeUUID)
                .cpuCapacity(CPUDTO.fromCPU(cpuResource.getCpuCapacity()))
                .cpuAllocatable(CPUDTO.fromCPU(cpuResource.getCpuAllocatable()))
                .build();
    }

}
