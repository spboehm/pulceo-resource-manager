package dev.pulceo.prm.service;

import dev.pulceo.prm.exception.ProviderServiceException;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.OnPremProviderRepository;
import dev.pulceo.prm.repository.ProviderMetaDataRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderService {

    private final Logger logger = LoggerFactory.getLogger(ProviderService.class);
    private final ProviderMetaDataRepository providerMetaDataRepository;
    private final OnPremProviderRepository onPremProviderRepository;
    private final AzureProviderRepository azureProviderRepository;

    @Autowired
    public ProviderService(ProviderMetaDataRepository providerMetaDataRepository, OnPremProviderRepository onPremProviderRepository, AzureProviderRepository azureProviderRepository) {
        this.providerMetaDataRepository = providerMetaDataRepository;
        this.onPremProviderRepository = onPremProviderRepository;
        this.azureProviderRepository = azureProviderRepository;
    }

    public AzureProvider createAzureProvider(AzureProvider azureProvider) throws ProviderServiceException {
        if (this.checkIfNameExists(azureProvider.getProviderMetaData().getProviderName())) {
            throw new ProviderServiceException("Provider with name " + azureProvider.getProviderMetaData().getProviderName() + " already exists");
        }
        return this.azureProviderRepository.save(azureProvider);
    }

    public OnPremProvider createOnPremProvider(OnPremProvider onPremProvider) throws ProviderServiceException {
        if (this.checkIfNameExists(onPremProvider.getProviderMetaData().getProviderName())) {
            throw new ProviderServiceException("Provider with name " + onPremProvider.getProviderMetaData().getProviderName() + " already exists");
        }
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

    public List<OnPremProvider> readAllOnPremProviders() {
        List<OnPremProvider> list = new ArrayList<>();
        Iterable<OnPremProvider> onPremProviders = this.onPremProviderRepository.findAll();
        onPremProviders.forEach( p-> {
            list.add(p);
        });
        return list;
    }

    public List<AzureProvider> readAllAzureProviders() {
        List<AzureProvider> list = new ArrayList<>();
        Iterable<AzureProvider> azureProviders = this.azureProviderRepository.findAll();
        azureProviders.forEach( p-> {
            list.add(p);
        });
        return list;
    }

    private boolean checkIfNameExists(String providerName) {
        return this.findProviderMetaDataByName(providerName).isPresent();
    }

    @PostConstruct
    public void initDefaultProvider() throws ProviderServiceException {
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

    public void deleteProviderByName(String id) throws ProviderServiceException {
        if (id.equals("default")) {
            throw new ProviderServiceException("Cannot delete default provider!");
        }
        Optional<ProviderMetaData> providerMetaData = this.findProviderMetaDataByName(id);
        if (providerMetaData.isPresent()) {
            if (providerMetaData.get().getProviderType() == ProviderType.ON_PREM) {
                Optional<OnPremProvider> onPremProvider = this.readOnPremProviderByProviderMetaData(providerMetaData.get());
                if (onPremProvider.isPresent()) {
                    this.onPremProviderRepository.delete(onPremProvider.get());
                }
            } else if (providerMetaData.get().getProviderType() == ProviderType.AZURE) {
                Optional<AzureProvider> azureProvider = this.readAzureProviderByProviderMetaData(providerMetaData.get());
                if (azureProvider.isPresent()) {
                    this.azureProviderRepository.delete(azureProvider.get());
                }
            }
        }
    }

    public void reset() {
        this.onPremProviderRepository.deleteAll();
        this.azureProviderRepository.deleteAll();
        this.providerMetaDataRepository.deleteAll();
        // Reinitialize the default provider
        try {
            this.initDefaultProvider();
        } catch (ProviderServiceException e) {
            this.logger.error("Error initializing default provider: {}", e.getMessage());
        }
    }
}
