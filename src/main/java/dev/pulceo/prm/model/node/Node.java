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
    private NodeType type = NodeType.EDGE;

    @Builder.Default
    @Min(1)
    @Max(16)
    private int layer = 1;

    @Builder.Default
    @NotNull(message="Node role is required!")
    private NodeRole role = NodeRole.WORKLOAD;

    @Builder.Default
    private String nodeGroup = "";

    @Builder.Default
    private String country = "";

    @Builder.Default
    private String state = "";

    @Builder.Default
    private String city = "";

    @Builder.Default
    @Min(-180)
    @Max(180)
    private double longitude = 0.000000;

    @Builder.Default
    @Min(-90)
    @Max(90)
    private double latitude = 0.000000;

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private CPUResource cpuResource = CPUResource.builder().build();

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private MemoryResource memoryResource= MemoryResource.builder().build();

    @Builder.Default
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private StorageResource storageResource = StorageResource.builder().build();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (layer != node.layer) return false;
        if (Double.compare(longitude, node.longitude) != 0) return false;
        if (Double.compare(latitude, node.latitude) != 0) return false;
        if (!Objects.equals(name, node.name)) return false;
        if (type != node.type) return false;
        if (role != node.role) return false;
        if (!Objects.equals(country, node.country))
            return false;
        if (!Objects.equals(city, node.city))
            return false;
        if (!Objects.equals(cpuResource, node.cpuResource)) return false;
        return Objects.equals(memoryResource, node.memoryResource);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + layer;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (cpuResource != null ? cpuResource.hashCode() : 0);
        result = 31 * result + (memoryResource != null ? memoryResource.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", layer=" + layer +
                ", role=" + role +
                ", nodeGroup='" + nodeGroup + '\'' +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", cpuResource=" + cpuResource +
                ", memoryResource=" + memoryResource +
                ", storageResource=" + storageResource +
                '}';
    }
}
