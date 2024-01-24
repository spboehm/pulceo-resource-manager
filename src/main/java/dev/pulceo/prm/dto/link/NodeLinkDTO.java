package dev.pulceo.prm.dto.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeLinkDTO {
    private UUID linkUuid;
    private String name;
    private UUID srcNodeUUID;
    private UUID destNodeUUID;
}
