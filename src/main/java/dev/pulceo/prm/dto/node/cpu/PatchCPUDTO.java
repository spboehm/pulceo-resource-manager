package dev.pulceo.prm.dto.node.cpu;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PatchCPUDTO {
    private String key;
    private float value;
}
