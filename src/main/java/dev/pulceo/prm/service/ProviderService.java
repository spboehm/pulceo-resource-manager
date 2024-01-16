package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.ProviderRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;

    @Autowired
    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public Provider createProvider(Provider provider) {
        return this.providerRepository.save(provider);
    }

    public Optional<Provider> readDefaultProvider() {
        return this.providerRepository.findByProviderName("default");
    }

    public Optional<Provider> readProviderByProviderName(String providerName) {
        return this.providerRepository.findByProviderName(providerName);
    }

    @PostConstruct
    public void initDefaultProvider() {
        // Check if a default provider already exists
        Optional<Provider> defaultProvider = this.providerRepository.findByProviderName("default");

        if (defaultProvider.isPresent()) {
            return;
        }

        this.createProvider(Provider.builder()
                .providerName("default")
                .providerType(ProviderType.ON_PREM)
                .build());
    }

}
