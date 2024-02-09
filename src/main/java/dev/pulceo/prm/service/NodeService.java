package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationRequestDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationResponseDTO;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NodeService {

    private final Logger logger = LoggerFactory.getLogger(NodeService.class);
    private final AbstractNodeRepository abstractNodeRepository;
    private final NodeMetaDataRepository nodeMetaDataRepository;
    private final OnPremNodeRepository onPremNoderepository;
    private final NodeRepository nodeRepository;
    private final ProviderService providerService;
    private final CloudRegistraionService cloudRegistraionService;
    private final CPUResourcesRepository cpuResourcesRepository;
    private final MemoryResourcesRepository memoryResourcesRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Value("${prm.uuid}")
    private UUID prmUUID;
    @Value("${prm.endpoint}")
    private String prmEndpoint;

    @Autowired
    public NodeService(AbstractNodeRepository abstractNodeRepository, OnPremNodeRepository onPremNoderepository, NodeMetaDataRepository nodeMetaDataRepository, NodeRepository nodeRepository, ProviderService providerService, CloudRegistraionService cloudRegistraionService, CPUResourcesRepository cpuResourcesRepository, MemoryResourcesRepository memoryResourcesRepository) {
        this.abstractNodeRepository = abstractNodeRepository;
        this.onPremNoderepository = onPremNoderepository;
        this.nodeMetaDataRepository = nodeMetaDataRepository;
        this.nodeRepository = nodeRepository;
        this.providerService = providerService;
        this.cloudRegistraionService = cloudRegistraionService;
        this.cpuResourcesRepository = cpuResourcesRepository;
        this.memoryResourcesRepository = memoryResourcesRepository;
    }

    public OnPremNode createOnPremNode(String providerName, String hostName, String pnaInitToken) throws NodeServiceException {
        Optional<OnPremProvider> onPremProvider = this.providerService.readOnPremProviderByProviderName(providerName);
        if (onPremProvider.isEmpty()) {
            throw new NodeServiceException("Provider does not exist!");
        }

        if (this.hostNameAlreadyExists(hostName)) {
            throw new NodeServiceException("Hostname already exists-");
        }

        CloudRegistrationRequestDTO cloudRegistrationRequestDTO = CloudRegistrationRequestDTO.builder()
                .prmUUID(this.prmUUID)
                .prmEndpoint(this.prmEndpoint)
                .pnaInitToken(pnaInitToken)
                .build();

        WebClient webClient = WebClient.create("http://" + hostName + ":7676");
        CloudRegistrationResponseDTO cloudRegistrationResponseDTO = webClient.post()
                .uri("/api/v1/cloud-registrations")
                .header("Content-Type", "application/json")
                .bodyValue(cloudRegistrationRequestDTO)
                .retrieve()
                .bodyToMono(CloudRegistrationResponseDTO.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to to issue a cloud registration"));
                })
                .block();

        CloudRegistration cloudRegistration = this.modelMapper.map(cloudRegistrationResponseDTO, CloudRegistration.class);
        logger.info("Received cloud registration response: " + cloudRegistration);

        // obtain remote resources
        CPUResource cpuResource = getCpuResource(webClient);
        MemoryResource memoryResource = getMemoryResource(webClient);

        Node node = Node.builder()
                .name(hostName)
                .cpuResource(cpuResource)
                .memoryResource(memoryResource)
                .build();

        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .remoteNodeUUID(cloudRegistration.getNodeUUID())
                .pnaUUID(cloudRegistration.getPnaUUID())
                .hostname(hostName)
                .build();

        OnPremNode onPremNode = OnPremNode.builder()
                .internalNodeType(InternalNodeType.ONPREM)
                .onPremProvider(onPremProvider.get())
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();

        return this.abstractNodeRepository.save(onPremNode);
    }

    private CPUResource getCpuResource(WebClient webClient) {
        CPUResourceDTO cpuResourceDTO = webClient.get()
                .uri("/api/v1/nodes/localNode/cpu")
                .retrieve()
                .bodyToMono(CPUResourceDTO.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to read CPU resources"));
                })
                .block();
        return this.modelMapper.map(cpuResourceDTO, CPUResource.class);
    }

    private MemoryResource getMemoryResource(WebClient webClient) {
        MemoryResourceDTO memoryResourceDTO = webClient.get()
                .uri("/api/v1/nodes/localNode/memory")
                .retrieve()
                .bodyToMono(MemoryResourceDTO.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to read Memory resources"));
                })
                .block();
        return this.modelMapper.map(memoryResourceDTO, MemoryResource.class);
    }

    @Transactional
    public List<AbstractNode> readAllNodes() {
        List<AbstractNode> listOfNodes = new ArrayList<>();
        this.abstractNodeRepository.findAll().forEach(listOfNodes::add);
        return listOfNodes;
    }

    public UUID getRemoteUUID(UUID localUUID) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(localUUID);
        if (abstractNode.isEmpty()) {
            throw new NodeServiceException("Node with UUID " + localUUID + " does not exist!");
        }
        NodeMetaData nodeMetaData = abstractNode.get().getNodeMetaData();
        return nodeMetaData.getRemoteNodeUUID();
    }

    @Transactional
    public OnPremNode readOnPremNode(Long id) {
        return this.onPremNoderepository.findById(id).get();
    }

    public CPUResource readCPUResourceByUUID(UUID nodeUUID) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(nodeUUID);
        if (abstractNode.isEmpty()) {
            throw new NodeServiceException("Node with UUID " + nodeUUID + " does not exist!");
        }
        InternalNodeType internalNodeType = abstractNode.get().getInternalNodeType();
        if (internalNodeType == InternalNodeType.ONPREM) {
            OnPremNode onPremNode = (OnPremNode) abstractNode.get();
            Long id = onPremNode.getNode().getCpuResource().getId();
            return this.cpuResourcesRepository.findById(id).orElseThrow();
        } else {
            throw new NodeServiceException("Node type not yet supported!");
        }
    }

    public MemoryResource readMemoryResourceByUUID(UUID nodeUUID) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(nodeUUID);
        if (abstractNode.isEmpty()) {
            throw new NodeServiceException("Node with UUID " + nodeUUID + " does not exist!");
        }
        InternalNodeType internalNodeType = abstractNode.get().getInternalNodeType();
        if (internalNodeType == InternalNodeType.ONPREM) {
            OnPremNode onPremNode = (OnPremNode) abstractNode.get();
            Long id = onPremNode.getNode().getCpuResource().getId();
            return this.memoryResourcesRepository.findById(id).orElseThrow();
        } else {
            throw new NodeServiceException("Node type not yet supported!");
        }
    }

    @Transactional
    public List<G6Node> readG6NodeData() {
        List<G6Node> list = new ArrayList<>();
        Iterable<AbstractNode> abstractNodeList = this.abstractNodeRepository.findAll();
        abstractNodeList.forEach(abstractNode -> {
           list.add(abstractNode.getG6Node());
        });
        return list;
    }

    public Optional<AbstractNode> readAbstractNodeByUUID(UUID uuid) {
        return this.abstractNodeRepository.findByUuid(uuid);
    }

    private boolean hostNameAlreadyExists(String hostName) throws NodeServiceException {
        return this.nodeMetaDataRepository.findByHostname(hostName).isPresent();
    }


}
