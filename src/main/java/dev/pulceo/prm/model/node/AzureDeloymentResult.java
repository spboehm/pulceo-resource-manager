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
public class AzureDeloymentResult extends BaseEntity {
    private String resourceGroupName;
    private String sku;
    private String fqdn;

    @Override
    public String toString() {
        return "AzureDeloymentResult{" +
                "resourceGroupName='" + resourceGroupName + '\'' +
                ", sku='" + sku + '\'' +
                ", fqdn='" + fqdn + '\'' +
                '}';
    }
}
