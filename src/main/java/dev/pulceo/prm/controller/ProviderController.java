package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.dto.provider.ProviderDTO;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.service.ProviderService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/azure-providers")
    public ResponseEntity<ProviderDTO> createAzureProvider(@Valid @RequestBody CreateNewAzureProviderDTO createNewAzureProviderDTO) {
        AzureProvider createdAzureProvider = this.providerService.createAzureProvider(AzureProvider.fromCreateNewAzureProviderDTO(createNewAzureProviderDTO));
        ProviderDTO providerDTO = ProviderDTO.fromAzureProvider(createdAzureProvider);
        return new ResponseEntity<>(providerDTO, HttpStatus.CREATED);
    }
//
//    @ExceptionHandler(value = DataIntegrityViolationException.class)
//    public ResponseEntity<CustomErrorResponse> handleCloudRegistrationException(DataIntegrityViolationException dataIntegrityViolationException) {
//        CustomErrorResponse error = new CustomErrorResponse("BAD_REQUEST", "A provider with these properties does already exist!");
//        error.setStatus(HttpStatus.BAD_REQUEST.value());
//        error.setTimestamp(LocalDateTime.now());
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }

}
