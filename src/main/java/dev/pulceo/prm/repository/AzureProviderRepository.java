package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface  AzureProviderRepository extends CrudRepository<AzureProvider, Long> {
    Optional<AzureProvider> findByProviderMetaData(ProviderMetaData providerMetaData);
}
