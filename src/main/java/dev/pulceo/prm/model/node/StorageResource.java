package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class StorageResource extends BaseEntity {

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Storage storageCapacity = Storage.builder().build();

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Storage storageAllocatable = Storage.builder().build();

    @Override
    public String toString() {
        return "StorageResource{" +
                "storageCapacity=" + storageCapacity +
                ", storageAllocatable=" + storageAllocatable +
                '}';
    }
}
