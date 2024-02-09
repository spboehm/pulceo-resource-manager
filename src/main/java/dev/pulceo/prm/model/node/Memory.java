package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class Memory extends BaseEntity {

    private float size;
    private int slots;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Memory memory = (Memory) o;

        if (Float.compare(size, memory.size) != 0) return false;
        return slots == memory.slots;
    }

    @Override
    public int hashCode() {
        int result = (size != 0.0f ? Float.floatToIntBits(size) : 0);
        result = 31 * result + slots;
        return result;
    }
}
