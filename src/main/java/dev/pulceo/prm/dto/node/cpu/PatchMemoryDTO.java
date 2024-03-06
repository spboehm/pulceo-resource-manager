package dev.pulceo.prm.dto.node.cpu;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PatchMemoryDTO {
    private String key;
    private float value;
}
