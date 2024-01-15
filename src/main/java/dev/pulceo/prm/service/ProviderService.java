package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderService {

    private ProviderRepository providerRepository;

    @Autowired
    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public Provider createProvider(Provider provider) {
        return this.providerRepository.save(provider);
    }

}
