package dev.pulceo.prm.dto.pna.node.CPU;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CPUResourceDTO {

    private CPUDTO cpuCapacity;
    private CPUDTO cpuAllocatable;

}
