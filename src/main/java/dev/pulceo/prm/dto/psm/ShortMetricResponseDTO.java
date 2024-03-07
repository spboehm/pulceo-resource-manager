package dev.pulceo.prm.dto.psm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO: used for internal, not for external, adjust naming
public class ShortMetricResponseDTO {
    // TODO: rename to jobUUID
    private UUID uuid;
    private UUID remoteMetricRequestUUID;
    private UUID linkUUID;
    private String type;
    private String recurrence;
    private boolean enabled;

}
