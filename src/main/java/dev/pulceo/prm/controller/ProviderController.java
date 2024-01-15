package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.provider.CreateNewProviderDTO;
import dev.pulceo.prm.dto.provider.ProviderDTO;
import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.service.ProviderService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("")
    public ResponseEntity<ProviderDTO> createProvider(@Valid @RequestBody CreateNewProviderDTO createNewProviderDTO) {
        Provider provider = this.modelMapper.map(createNewProviderDTO, Provider.class);
        Provider createdProvider = this.providerService.createProvider(provider);
        return new ResponseEntity<>(this.modelMapper.map(createdProvider, ProviderDTO.class), org.springframework.http.HttpStatus.CREATED);
    }
}
