package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
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
public class OnPremNode extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private OnPremProvider onPremProvider;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NodeMetaData nodeMetaData;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Node node;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private CloudRegistration cloudRegistration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnPremNode that = (OnPremNode) o;

        if (!Objects.equals(onPremProvider, that.onPremProvider))
            return false;
        if (!Objects.equals(nodeMetaData, that.nodeMetaData)) return false;
        if (!Objects.equals(node, that.node)) return false;
        return Objects.equals(cloudRegistration, that.cloudRegistration);
    }

    @Override
    public int hashCode() {
        int result = onPremProvider != null ? onPremProvider.hashCode() : 0;
        result = 31 * result + (nodeMetaData != null ? nodeMetaData.hashCode() : 0);
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (cloudRegistration != null ? cloudRegistration.hashCode() : 0);
        return result;
    }
}
