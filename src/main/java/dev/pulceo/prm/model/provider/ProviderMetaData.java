package dev.pulceo.prm.model.provider;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class ProviderMetaData extends BaseEntity {

    private String providerName;
    private ProviderType providerType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderMetaData that = (ProviderMetaData) o;

        if (!Objects.equals(providerName, that.providerName)) return false;
        return providerType == that.providerType;
    }

    @Override
    public int hashCode() {
        int result = providerName != null ? providerName.hashCode() : 0;
        result = 31 * result + (providerType != null ? providerType.hashCode() : 0);
        return result;
    }
}
