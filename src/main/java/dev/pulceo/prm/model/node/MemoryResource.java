package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class MemoryResource extends BaseEntity {

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Memory memoryCapacity = Memory.builder().build();

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Memory memoryAllocatable = Memory.builder().build();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryResource that = (MemoryResource) o;

        if (!Objects.equals(memoryCapacity, that.memoryCapacity))
            return false;
        return Objects.equals(memoryAllocatable, that.memoryAllocatable);
    }

    @Override
    public int hashCode() {
        int result = memoryCapacity != null ? memoryCapacity.hashCode() : 0;
        result = 31 * result + (memoryAllocatable != null ? memoryAllocatable.hashCode() : 0);
        return result;
    }
}
