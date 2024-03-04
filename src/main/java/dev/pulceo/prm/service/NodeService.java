package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationRequestDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationResponseDTO;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final AzureDeploymentService azureDeploymentService;
    private final AzureNodeRepository azureNodeRepository;


    @Value("${prm.uuid}")
    private UUID prmUUID;
    @Value("${prm.endpoint}")
    private String prmEndpoint;
    @Value("${webclient.scheme}")
    private String webClientScheme;
    @Value("${PNA_INIT_TOKEN}")
    private String pnaInitToken;

    @Autowired
    public NodeService(AbstractNodeRepository abstractNodeRepository, OnPremNodeRepository onPremNoderepository, NodeMetaDataRepository nodeMetaDataRepository, NodeRepository nodeRepository, ProviderService providerService, CloudRegistraionService cloudRegistraionService, CPUResourcesRepository cpuResourcesRepository, MemoryResourcesRepository memoryResourcesRepository, AzureDeploymentService azureDeploymentService, AzureNodeRepository azureNodeRepository) {
        this.abstractNodeRepository = abstractNodeRepository;
        this.onPremNoderepository = onPremNoderepository;
        this.nodeMetaDataRepository = nodeMetaDataRepository;
        this.nodeRepository = nodeRepository;
        this.providerService = providerService;
        this.cloudRegistraionService = cloudRegistraionService;
        this.cpuResourcesRepository = cpuResourcesRepository;
        this.memoryResourcesRepository = memoryResourcesRepository;
        this.azureDeploymentService = azureDeploymentService;
        this.azureNodeRepository = azureNodeRepository;
    }

    public OnPremNode createOnPremNode(String name, String providerName, String hostName, String pnaInitToken) throws NodeServiceException {
        Optional<OnPremProvider> onPremProvider = this.providerService.readOnPremProviderByProviderName(providerName);
        if (onPremProvider.isEmpty()) {
            throw new NodeServiceException("Provider does not exist!");
        }

        if (this.hostNameAlreadyExists(hostName)) {
            throw new NodeServiceException("Hostname already exists-");
        }

        if (this.nameAlreadyExists(name)) {
            throw new NodeServiceException("Name already exists-");
        }

        CloudRegistrationRequestDTO cloudRegistrationRequestDTO = CloudRegistrationRequestDTO.builder()
                .prmUUID(this.prmUUID)
                .prmEndpoint(this.prmEndpoint)
                .pnaInitToken(pnaInitToken)
                .build();

        WebClient webClient = WebClient.create(this.webClientScheme + "://" + hostName + ":7676");
        CloudRegistrationResponseDTO cloudRegistrationResponseDTO = webClient.post()
                .uri("/api/v1/cloud-registrations")
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + pnaInitToken)
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
        CPUResource cpuResource = getCpuResource(webClient, pnaInitToken);
        MemoryResource memoryResource = getMemoryResource(webClient, pnaInitToken);

        Node node = Node.builder()
                .name(name)
                .cpuResource(cpuResource)
                .memoryResource(memoryResource)
                .build();

        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .remoteNodeUUID(cloudRegistration.getNodeUUID())
                .pnaUUID(cloudRegistration.getPnaUUID())
                .hostname(hostName)
                .build();

        OnPremNode onPremNode = OnPremNode.builder()
                .name(name)
                .internalNodeType(InternalNodeType.ONPREM)
                .onPremProvider(onPremProvider.get())
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();

        return this.abstractNodeRepository.save(onPremNode);
    }

    public AzureNode createPreliminaryAzureNode(CreateNewAzureNodeDTO createNewAzureNodeDTO) throws NodeServiceException {
        Optional<AzureProvider> azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(createNewAzureNodeDTO.getProviderName());
        if (azureProvider.isEmpty()) {
            throw new NodeServiceException("Provider does not exist!");
        }

        if (this.nameAlreadyExists(createNewAzureNodeDTO.getName())) {
            throw new NodeServiceException("Name already exists-");
        }

        // due to random hostname creation, duplicate hostnames will never happen

        Node node = Node.builder()
                .name(createNewAzureNodeDTO.getName())
                .type(NodeType.valueOf(createNewAzureNodeDTO.getType().toUpperCase())) // TODO: replace
                .nodeLocationCountry(this.getCountryByRegion(createNewAzureNodeDTO.getNodeLocationCountry()))
                .nodeLocationCity(this.getCityByRegion(createNewAzureNodeDTO.getNodeLocationCity()))
                .build();

        AzureNode azureNode = AzureNode.builder()
                .name(createNewAzureNodeDTO.getName())
                .internalNodeType(InternalNodeType.AZURE)
                .azureProvider(azureProvider.get())
                .node(node)
                .nodeMetaData(NodeMetaData.builder().build())
                .build();

        return this.azureNodeRepository.save(azureNode);
    }

    @Async
    public CompletableFuture<AzureNode> createAzureNodeAsync(UUID nodeUuid, CreateNewAzureNodeDTO createNewAzureNodeDTO) throws NodeServiceException {
        // TODO: find by name
        logger.info("Received async request for creating azure node with uuid %s " + " with " + createNewAzureNodeDTO);
        Optional<AzureNode> azureNode = this.readAzureNodeByUUID(nodeUuid);
        if (azureNode.isEmpty()) {
            throw new NodeServiceException("Node with uuid %s does not exist. Please create at first lazily");
        }
        // TODO: further validations

        // TODO: String providerName, String hostName, String pnaInitToken

        try {
            // invoke AzureDeploymentService for creation of VM
            AzureDeloymentResult azureDeloymentResult = this.azureDeploymentService.deploy(createNewAzureNodeDTO.getProviderName(), createNewAzureNodeDTO.getNodeLocationCountry(), createNewAzureNodeDTO.getSku());
            logger.info("Received azure deployment response: " + azureDeloymentResult.toString());
            // TODO: poll with /health until available, or alternatively, wait until completion with events
            // TODO: replace with https or make configuration
            logger.info("Waiting for 15 seconds for Let's Encrypt to issue a certificate...");
            Thread.sleep(15000);
            logger.info("Waiting for node to be available...");
            WebClient webClientToAzureNode = WebClient.create(this.webClientScheme + "://" + azureDeloymentResult.getFqdn() + ":7676");
            webClientToAzureNode.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(6, Duration.ofSeconds(10)))
                    .block();

            CloudRegistrationRequestDTO cloudRegistrationRequestDTO = CloudRegistrationRequestDTO.builder()
                    .prmUUID(this.prmUUID)
                    .prmEndpoint(this.prmEndpoint)
                    .pnaInitToken(this.pnaInitToken)
                    .build();

            logger.info("Issuing CloudRegistrationRequestDTO to AzureNode: " + cloudRegistrationRequestDTO.toString());
            CloudRegistrationResponseDTO cloudRegistrationResponseDTO = webClientToAzureNode.post()
                    .uri("/api/v1/cloud-registrations")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + this.pnaInitToken)
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
            CPUResource cpuResource = getCpuResource(webClientToAzureNode, pnaInitToken);
            MemoryResource memoryResource = getMemoryResource(webClientToAzureNode, pnaInitToken);

            // update azure node
            AzureNode azureNodeToBeUpdated = azureNode.get();

            // complete dynamically obtained data from node
            azureNodeToBeUpdated.getNode().setCpuResource(cpuResource);
            azureNodeToBeUpdated.getNode().setMemoryResource(memoryResource);

            azureNodeToBeUpdated.getNodeMetaData().setRemoteNodeUUID(cloudRegistration.getNodeUUID());
            azureNodeToBeUpdated.getNodeMetaData().setPnaUUID(cloudRegistration.getPnaUUID());
            azureNodeToBeUpdated.getNodeMetaData().setHostname(azureDeloymentResult.getFqdn());

            // generate nodeMetaData
//            NodeMetaData nodeMetaData = NodeMetaData.builder()
//                    .remoteNodeUUID(cloudRegistration.getNodeUUID())
//                    .pnaUUID(cloudRegistration.getPnaUUID())
//                    .hostname(azureDeloymentResult.getFqdn())
//                    .build();

            // set NodeMetaData, CloudRegistration, AzureDeloymentResult
//            azureNodeToBeUpdated.setNodeMetaData(nodeMetaData);
            azureNodeToBeUpdated.setCloudRegistration(cloudRegistration);
            azureNodeToBeUpdated.setAzureDeloymentResult(azureDeloymentResult);
            this.azureNodeRepository.save(azureNodeToBeUpdated);
            return CompletableFuture.completedFuture(azureNodeToBeUpdated);
        } catch (AzureDeploymentServiceException e) {
            throw new NodeServiceException("Could not create azure node!", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCityByRegion(String region) {
        if (region.equals("eastus")) {
            return "Virginia";
        } else {
            return "";
        }
    }

    private String getCountryByRegion(String region) {
        if (region.equals("eastus")) {
            return "USA";
        } else {
            return "";
        }
    }

    private CPUResource getCpuResource(WebClient webClient, String pnaInitToken) {
        CPUResourceDTO cpuResourceDTO = webClient.get()
                .uri("/api/v1/nodes/localNode/cpu")
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + pnaInitToken)
                .retrieve()
                .bodyToMono(CPUResourceDTO.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to read CPU resources"));
                })
                .block();
        return this.modelMapper.map(cpuResourceDTO, CPUResource.class);
    }

    private MemoryResource getMemoryResource(WebClient webClient, String pnaInitToken) {
        MemoryResourceDTO memoryResourceDTO = webClient.get()
                .uri("/api/v1/nodes/localNode/memory")
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + pnaInitToken)
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

    @Transactional
    public AzureNode readAzureNode(Long id) {
        return this.azureNodeRepository.findById(id).get();
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

    public Optional<AbstractNode> readAbstractNodeByName(String name) {
        return this.abstractNodeRepository.findByName(name);
    }

    private boolean hostNameAlreadyExists(String hostName) throws NodeServiceException {
        return this.nodeMetaDataRepository.findByHostname(hostName).isPresent();
    }

    private boolean nameAlreadyExists(String name) {
        return this.abstractNodeRepository.findByName(name).isPresent();
    }

    public Optional<AzureNode> readAzureNodeByUUID(UUID uuid) {
        return this.azureNodeRepository.readAzureNodeByUuid(uuid);
    }


}
