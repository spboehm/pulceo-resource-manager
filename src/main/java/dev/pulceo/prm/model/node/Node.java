package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class Node extends BaseEntity {

    @NotBlank(message="Name is required!")
    private String name;

    @Builder.Default
    @NotNull(message = "Node type is required!")
    private NodeType type = NodeType.EDGE;

    @Builder.Default
    @Min(1)
    @Max(16)
    private int layer = 1;

    @Builder.Default
    @NotNull(message="Node role is required!")
    private NodeRole role = NodeRole.WORKLOAD;

    @Builder.Default
    @NotNull
    private String nodeLocationCountry = "";

    @Builder.Default
    @NotNull
    private String nodeLocationCity = "";

    @Builder.Default
    @Min(-180)
    @Max(180)
    private double nodeLocationLongitude = 0.000000;

    @Builder.Default
    @Min(-90)
    @Max(90)
    private double nodeLocationLatitude = 0.000000;

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private CPUResource cpuResource = CPUResource.builder().build();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (layer != node.layer) return false;
        if (Double.compare(nodeLocationLongitude, node.nodeLocationLongitude) != 0) return false;
        if (Double.compare(nodeLocationLatitude, node.nodeLocationLatitude) != 0) return false;
        if (!Objects.equals(name, node.name)) return false;
        if (type != node.type) return false;
        if (role != node.role) return false;
        if (!Objects.equals(nodeLocationCountry, node.nodeLocationCountry))
            return false;
        if (!Objects.equals(nodeLocationCity, node.nodeLocationCity))
            return false;
        return Objects.equals(cpuResource, node.cpuResource);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + layer;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (nodeLocationCountry != null ? nodeLocationCountry.hashCode() : 0);
        result = 31 * result + (nodeLocationCity != null ? nodeLocationCity.hashCode() : 0);
        temp = Double.doubleToLongBits(nodeLocationLongitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nodeLocationLatitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (cpuResource != null ? cpuResource.hashCode() : 0);
        return result;
    }
}
