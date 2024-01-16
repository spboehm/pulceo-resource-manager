package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.provider.AzureProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  AzureProviderRepository extends CrudRepository<AzureProvider, Long> {
}
