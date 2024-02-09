package dev.pulceo.prm.dto.pna.node.memory;

import dev.pulceo.prm.model.node.MemoryResource;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoryResourceDTO {
    private UUID uuid;
    private UUID nodeUUID;
    private String nodeName;
    private MemoryDTO memoryCapacity;
    private MemoryDTO memoryAllocatable;

    public static MemoryResourceDTO fromMemoryResource(UUID nodeUUID, String nodeName, MemoryResource memoryResource) {
        return MemoryResourceDTO.builder()
                .uuid(memoryResource.getUuid())
                .nodeName(nodeName)
                .nodeUUID(nodeUUID)
                .memoryCapacity(MemoryDTO.fromMemory(memoryResource.getMemoryCapacity()))
                .memoryAllocatable(MemoryDTO.fromMemory(memoryResource.getMemoryAllocatable()))
                .build();
    }
}
