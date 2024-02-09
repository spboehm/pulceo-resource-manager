package dev.pulceo.prm.dto.pna.node.memory;

import dev.pulceo.prm.model.node.Memory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoryDTO {
    private float size;
    private int slots;

    public static MemoryDTO fromMemory(Memory memory) {
        return MemoryDTO.builder()
                .size(memory.getSize())
                .slots(memory.getSlots())
                .build();
    }
}
