package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.provider.Provider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends CrudRepository<Provider, Long> {

    Optional<Provider> findByProviderName(String providerName);

}
