package dev.pulceo.prm.model.provider;

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
@ToString
public class ProviderMetaData extends BaseEntity {

    private String providerName;
    private ProviderType providerType;

}
