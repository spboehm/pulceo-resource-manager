package dev.pulceo.prm.model.provider;

import dev.pulceo.prm.model.BaseEntity;
import dev.pulceo.prm.model.node.OnPremNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class OnPremProvider extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ProviderMetaData providerMetaData;
    @OneToMany(mappedBy = "onPremProvider")
    private List<OnPremNode> onPremNodes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnPremProvider that = (OnPremProvider) o;

        return Objects.equals(providerMetaData, that.providerMetaData);
    }

    @Override
    public int hashCode() {
        return providerMetaData != null ? providerMetaData.hashCode() : 0;
    }
}
