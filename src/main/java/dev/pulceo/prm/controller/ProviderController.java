package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.dto.provider.ProviderDTO;
import dev.pulceo.prm.exception.ProviderServiceException;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.service.ProviderService;
import jakarta.validation.Valid;
import org.modelmapper.AbstractProvider;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;
    private final ModelMapper modelMapper;

    @Autowired
    public ProviderController(ProviderService providerService, ModelMapper modelMapper) {
        this.providerService = providerService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    // TODO: add polymorphism
    public ResponseEntity<ProviderDTO> createAzureProvider(@Valid @RequestBody CreateNewAzureProviderDTO createNewAzureProviderDTO) throws ProviderServiceException {
        AzureProvider createdAzureProvider = this.providerService.createAzureProvider(AzureProvider.fromCreateNewAzureProviderDTO(createNewAzureProviderDTO));
        ProviderDTO providerDTO = ProviderDTO.fromAzureProvider(createdAzureProvider);
        return new ResponseEntity<>(providerDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderDTO> readProviderById(@PathVariable String id) {
        Optional<ProviderMetaData> providerMetaData = this.providerService.findProviderMetaDataByName(id);
        if (providerMetaData.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        if (providerMetaData.get().getProviderType() == ProviderType.AZURE) {
            Optional<AzureProvider> azureProvider = this.providerService.readAzureProviderByProviderMetaData(providerMetaData.get());
            if (azureProvider.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(ProviderDTO.fromAzureProvider(azureProvider.get()));
        } else if (providerMetaData.get().getProviderType() == ProviderType.ON_PREM) {
            Optional<OnPremProvider> onPremNode = this.providerService.readOnPremProviderByProviderMetaData(providerMetaData.get());
            if (onPremNode.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(ProviderDTO.fromOnPremProvider(onPremNode.get()));
        }
        return ResponseEntity.status(404).build();
    }

    @GetMapping("")
    public List<ProviderDTO> readAllProviders() {
        List<OnPremProvider> onPremProviders = this.providerService.readAllOnPremProviders();
        List<AzureProvider> azureProviders = this.providerService.readAllAzureProviders();
        List<ProviderDTO> providerDTOs = new ArrayList<>();
        for (OnPremProvider onPremProvider : onPremProviders) {
            providerDTOs.add(ProviderDTO.fromOnPremProvider(onPremProvider));
        }
        for (AzureProvider azureProvider : azureProviders) {
            providerDTOs.add(ProviderDTO.fromAzureProvider(azureProvider));
        }
        return providerDTOs;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteProviderById(@PathVariable String id) throws ProviderServiceException {
        this.providerService.deleteProviderByName(id);
    }

    @ExceptionHandler(value = ProviderServiceException.class)
    public ResponseEntity<CustomErrorResponse> handleCloudRegistrationException(ProviderServiceException providerServiceException) {
        CustomErrorResponse error = new CustomErrorResponse("BAD_REQUEST", providerServiceException.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setErrorMsg(providerServiceException.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
