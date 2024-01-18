package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.registration.CloudRegistrationRequestDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationResponseDTO;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.NodeRepository;
import dev.pulceo.prm.repository.OnPremNoderepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
public class NodeService {

    private final OnPremNoderepository onPremNoderepository;
    private final NodeRepository nodeRepository;

    private final ProviderService providerService;

    private final ModelMapper modelMapper = new ModelMapper();

    @Value("${prm.uuid}")
    private String prmUUID;
    @Value("${prm.endpoint}")
    private String prmEndpoint;

    @Autowired
    public NodeService(OnPremNoderepository onPremNoderepository, NodeRepository nodeRepository, ProviderService providerService) {
        this.onPremNoderepository = onPremNoderepository;
        this.nodeRepository = nodeRepository;
        this.providerService = providerService;
    }

    public OnPremNode createOnPremNode(String providerName, String hostName, String pnaInitToken) {

        // TODO: look if provider does exist
        // Goal: find OnPremProvider
        Optional<OnPremProvider> onPremProvider = this.providerService.readOnPremProviderByProviderName(providerName);

        // TODO: check if the node with the hostname already exists in OnPremNode#NodeMetaData
        // TOOD: Issue request with CloudRegistrationRequestDTO
        CloudRegistrationRequestDTO cloudRegistrationRequestDTO = CloudRegistrationRequestDTO.builder()
                .prmUUID(this.prmUUID)
                .prmEndpoint(this.prmEndpoint)
                .pnaInitToken(pnaInitToken)
                .build();

        WebClient webClient = WebClient.create("http://" + hostName + ":7676");
        CloudRegistrationResponseDTO cloudRegistrationResponseDTO = webClient.post()
                .uri("/api/v1/cloud-registrations")
                .bodyValue(cloudRegistrationRequestDTO)
                .retrieve()
                .bodyToMono(CloudRegistrationResponseDTO.class)
                .block();

        // TODO: Transform to CloudRegistration
        CloudRegistration cloudRegistration = this.modelMapper.map(cloudRegistrationResponseDTO, CloudRegistration.class);

        // Build a CloudRegistrationRequestDTO json with prmUUID, prmEndpoint, and pnaInitToken

        return null;
    }


}
