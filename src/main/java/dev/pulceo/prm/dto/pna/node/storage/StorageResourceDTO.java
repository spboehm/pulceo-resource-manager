package dev.pulceo.prm.dto.pna.node.storage;

import dev.pulceo.prm.dto.pna.node.memory.MemoryDTO;
import dev.pulceo.prm.model.node.StorageResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageResourceDTO {
    private UUID uuid;
    private UUID nodeUUID;
    private String nodeName;
    private StorageDTO storageCapacity;
    private StorageDTO storageAllocatable;

    public static StorageResourceDTO fromStorageResource(UUID nodeUUID, String nodeName, StorageResource storageResource) {
        return StorageResourceDTO.builder()
                .uuid(storageResource.getUuid())
                .nodeName(nodeName)
                .nodeUUID(nodeUUID)
                .storageCapacity(StorageDTO.fromStorage(storageResource.getStorageCapacity()))
                .storageAllocatable(StorageDTO.fromStorage(storageResource.getStorageAllocatable()))
                .build();
    }
}
