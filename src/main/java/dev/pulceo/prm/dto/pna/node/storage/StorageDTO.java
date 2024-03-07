package dev.pulceo.prm.dto.pna.node.storage;

import dev.pulceo.prm.model.node.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageDTO {
    private float size;
    private int slots;

    public static StorageDTO fromStorage(Storage storage) {
        return StorageDTO.builder()
                .size(storage.getSize())
                .slots(storage.getSlots())
                .build();
    }
}
