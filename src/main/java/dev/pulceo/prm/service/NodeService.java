package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.registration.CloudRegistrationRequestDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationResponseDTO;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeMetaData;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.NodeMetaDataRepository;
import dev.pulceo.prm.repository.NodeRepository;
import dev.pulceo.prm.repository.OnPremNodeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;

@Service
public class NodeService {

    private final AbstractNodeRepository abstractNodeRepository;
    private final NodeMetaDataRepository nodeMetaDataRepository;
    private final OnPremNodeRepository onPremNoderepository;
    private final NodeRepository nodeRepository;

    private final ProviderService providerService;
    private final CloudRegistraionService cloudRegistraionService;

    private final ModelMapper modelMapper = new ModelMapper();

    @Value("${prm.uuid}")
    private UUID prmUUID;
    @Value("${prm.endpoint}")
    private String prmEndpoint;

    @Autowired
    public NodeService(AbstractNodeRepository abstractNodeRepository, OnPremNodeRepository onPremNoderepository, NodeMetaDataRepository nodeMetaDataRepository, NodeRepository nodeRepository, ProviderService providerService, CloudRegistraionService cloudRegistraionService) {
        this.abstractNodeRepository = abstractNodeRepository;
        this.onPremNoderepository = onPremNoderepository;
        this.nodeMetaDataRepository = nodeMetaDataRepository;
        this.nodeRepository = nodeRepository;
        this.providerService = providerService;
        this.cloudRegistraionService = cloudRegistraionService;
    }

    public OnPremNode createOnPremNode(String providerName, String hostName, String pnaInitToken) throws NodeServiceException {

        Optional<OnPremProvider> onPremProvider = this.providerService.readOnPremProviderByProviderName(providerName);
        if (onPremProvider.isEmpty()) {
            throw new NodeServiceException("Provider does not exist");
        }

        if (this.hostNameAlreadyExists(hostName)) {
            throw new NodeServiceException("Hostname already exists");
        }

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

        CloudRegistration cloudRegistration = this.modelMapper.map(cloudRegistrationResponseDTO, CloudRegistration.class);

        Node node = Node.builder().name(hostName).build();

        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .pnaUUID(cloudRegistration.getPnaUUID())
                .hostname(hostName)
                .build();

        OnPremNode onPremNode = OnPremNode.builder()
                .onPremProvider(onPremProvider.get())
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();

        return this.abstractNodeRepository.save(onPremNode);
    }

    private boolean hostNameAlreadyExists(String hostName) throws NodeServiceException {
        return this.nodeMetaDataRepository.findByHostname(hostName).isPresent();
    }

}
