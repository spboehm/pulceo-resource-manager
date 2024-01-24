package dev.pulceo.prm.dto.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewNodeLinkDTO {
    private String name;
    private UUID srcNodeUUID;
    private UUID destNodeUUID;
}
