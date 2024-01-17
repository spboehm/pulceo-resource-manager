package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OnPremProviderRepository extends CrudRepository<OnPremProvider, Long> {

    Optional<OnPremProvider> findByProviderMetaData(ProviderMetaData providerMetaData);

}
