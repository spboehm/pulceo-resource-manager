package dev.pulceo.prm.dto.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PatchNodeDTO {
    private String key;
    private String value;
}
