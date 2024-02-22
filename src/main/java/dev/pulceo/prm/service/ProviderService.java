package dev.pulceo.prm.service;

import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.OnPremProviderRepository;
import dev.pulceo.prm.repository.ProviderMetaDataRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProviderService {

    private final ProviderMetaDataRepository providerMetaDataRepository;
    private final OnPremProviderRepository onPremProviderRepository;
    private final AzureProviderRepository azureProviderRepository;

    @Autowired
    public ProviderService(ProviderMetaDataRepository providerMetaDataRepository, OnPremProviderRepository onPremProviderRepository, AzureProviderRepository azureProviderRepository) {
        this.providerMetaDataRepository = providerMetaDataRepository;
        this.onPremProviderRepository = onPremProviderRepository;
        this.azureProviderRepository = azureProviderRepository;
    }

    public AzureProvider createAzureProvider(AzureProvider azureProvider) {
        return this.azureProviderRepository.save(azureProvider);
    }

    public OnPremProvider createOnPremProvider(OnPremProvider onPremProvider) {
        return this.onPremProviderRepository.save(onPremProvider);
    }

    public Optional<ProviderMetaData> findProviderMetaDataByName(String name) {
        return this.providerMetaDataRepository.findByProviderName(name);
    }

    public Optional<OnPremProvider> readOnPremProviderByProviderMetaData(ProviderMetaData providerMetaData) {
        return this.onPremProviderRepository.findByProviderMetaData(providerMetaData);
    }

    public Optional<OnPremProvider> readOnPremProviderByProviderName(String providerName) {
        Optional<ProviderMetaData> providerMetaData = this.findProviderMetaDataByName(providerName);

        if (providerMetaData.isPresent()) {
            return this.readOnPremProviderByProviderMetaData(providerMetaData.get());
        }

        return Optional.empty();
    }

    public Optional<AzureProvider> readAzureProviderByProviderMetaData(ProviderMetaData providerMetaData) {
        return this.azureProviderRepository.findByProviderMetaData(providerMetaData);
    }

    public Optional<AzureProvider> readAzureProviderByProviderMetaDataName(String providerName) {
        Optional<ProviderMetaData> providerMetaData = this.findProviderMetaDataByName(providerName);

        if (providerMetaData.isPresent()) {
            return this.readAzureProviderByProviderMetaData(providerMetaData.get());
        }

        return Optional.empty();
    }

    @PostConstruct
    public void initDefaultProvider() {
        // Check if a default provider already exists
        Optional<ProviderMetaData> providerMetaData = this.findProviderMetaDataByName("default");

        if (providerMetaData.isEmpty()) {
            ProviderMetaData defaultProviderMetaData = ProviderMetaData.builder()
                    .providerName("default")
                    .providerType(ProviderType.ON_PREM)
                    .build();

            OnPremProvider defaultOnPremProvider = OnPremProvider.builder()
                    .providerMetaData(defaultProviderMetaData)
                    .build();

            this.createOnPremProvider(defaultOnPremProvider);
        }
    }

}
