package dev.pulceo.prm.model.provider;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
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
@ToString
public class OnPremProvider extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ProviderMetaData providerMetaData;
}
