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
public class CPUResource extends BaseEntity {

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private CPU cpuCapacity = CPU.builder().build();

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private CPU cpuAllocatable = CPU.builder().build();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPUResource that = (CPUResource) o;

        if (!Objects.equals(cpuCapacity, that.cpuCapacity)) return false;
        return Objects.equals(cpuAllocatable, that.cpuAllocatable);
    }

    @Override
    public int hashCode() {
        int result = cpuCapacity != null ? cpuCapacity.hashCode() : 0;
        result = 31 * result + (cpuAllocatable != null ? cpuAllocatable.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CPUResource{" +
                "cpuCapacity=" + cpuCapacity +
                ", cpuAllocatable=" + cpuAllocatable +
                '}';
    }
}
