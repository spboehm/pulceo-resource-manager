package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.dto.pna.node.storage.StorageResourceDTO;
import dev.pulceo.prm.dto.psm.ApplicationDTO;
import dev.pulceo.prm.dto.psm.CreateNewApplicationDTO;
import dev.pulceo.prm.dto.psm.ShortMetricResponseDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationRequestDTO;
import dev.pulceo.prm.dto.registration.CloudRegistrationResponseDTO;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.model.event.EventType;
import dev.pulceo.prm.model.event.PulceoEvent;
import dev.pulceo.prm.model.link.AbstractLink;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
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
    private final StorageResourcesRepositoy storageResourcesRepositoy;
    private final ModelMapper modelMapper = new ModelMapper();
    private final AzureDeploymentService azureDeploymentService;
    private final AzureNodeRepository azureNodeRepository;
    private final LinkService linkService;
    private final EventHandler eventHandler;

    @Value("${prm.uuid}")
    private UUID prmUUID;
    @Value("${prm.endpoint}")
    private String prmEndpoint;
    @Value("${webclient.scheme}")
    private String webClientScheme;
    @Value("${pna.init.token}")
    private String pnaInitToken;
    @Value("${pms.endpoint}")
    private String pmsEndpoint;
    @Value("${pms.mqtt.topic}")
    private String pmsMqttTopic;
    @Value("${psm.endpoint}")
    private String psmEndpoint;

    @Autowired
    public NodeService(AbstractNodeRepository abstractNodeRepository, OnPremNodeRepository onPremNoderepository, NodeMetaDataRepository nodeMetaDataRepository, NodeRepository nodeRepository, ProviderService providerService, CloudRegistraionService cloudRegistraionService, CPUResourcesRepository cpuResourcesRepository, MemoryResourcesRepository memoryResourcesRepository, StorageResourcesRepositoy storageResourcesRepositoy, AzureDeploymentService azureDeploymentService, AzureNodeRepository azureNodeRepository, @Lazy LinkService linkService, EventHandler eventHandler) {
        this.abstractNodeRepository = abstractNodeRepository;
        this.onPremNoderepository = onPremNoderepository;
        this.nodeMetaDataRepository = nodeMetaDataRepository;
        this.nodeRepository = nodeRepository;
        this.providerService = providerService;
        this.cloudRegistraionService = cloudRegistraionService;
        this.cpuResourcesRepository = cpuResourcesRepository;
        this.memoryResourcesRepository = memoryResourcesRepository;
        this.storageResourcesRepositoy = storageResourcesRepositoy;
        this.azureDeploymentService = azureDeploymentService;
        this.azureNodeRepository = azureNodeRepository;
        this.linkService = linkService;
        this.eventHandler = eventHandler;
    }

    public OnPremNode createOnPremNode(String name, String providerName, String hostName, String pnaInitToken, String type, String country, String state, String city) throws NodeServiceException, InterruptedException {
        Optional<OnPremProvider> onPremProvider = this.providerService.readOnPremProviderByProviderName(providerName);
        if (onPremProvider.isEmpty()) {
            throw new NodeServiceException("Provider does not exist!");
        }

        if (this.hostNameAlreadyExists(hostName)) {
            throw new NodeServiceException("Hostname already exists!");
        }

        if (this.nameAlreadyExists(name)) {
            throw new NodeServiceException("Name already exists!");
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
        StorageResource storageResource = getStorageResource(webClient, pnaInitToken);

        Node node = Node.builder()
                .name(name)
                .type(NodeType.valueOf(type.toUpperCase())) // TODO: replace
                .country(country)
                .state(state)
                .city(city)
                .cpuResource(cpuResource)
                .memoryResource(memoryResource)
                .storageResource(storageResource)
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

        OnPremNode readyOnPremNode = this.abstractNodeRepository.save(onPremNode);

        // application dummy
        WebClient webClientToPSM = WebClient.create(this.psmEndpoint);
        CreateNewApplicationDTO pulceoNodeAgentDTO = CreateNewApplicationDTO.builder()
                .nodeId(onPremNode.getUuid().toString())
                .name("pulceo-node-agent")
                .build();

        webClientToPSM.post()
                .uri("/api/v1/applications")
                .header("Content-Type", "application/json")
                .bodyValue(pulceoNodeAgentDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to create application"));
                })
                .block();

        CreateNewApplicationDTO traefikDTO = CreateNewApplicationDTO.builder()
                .nodeId(onPremNode.getUuid().toString())
                .name("traefik")
                .build();

        webClientToPSM.post()
                .uri("/api/v1/applications")
                .header("Content-Type", "application/json")
                .bodyValue(traefikDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to create application"));
                })
                .block();

        logger.info("Successfully created on-prem node: " + onPremNode.toString());
        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.NODE_CREATED)
                .payload(onPremNode.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);
        return readyOnPremNode;
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
                .country(this.getCountryByRegion(createNewAzureNodeDTO.getRegion()))
                .state(this.getStateByRegion(createNewAzureNodeDTO.getRegion()))
                .city(this.getCityByRegion(createNewAzureNodeDTO.getRegion()))
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
            AzureDeloymentResult azureDeloymentResult = this.azureDeploymentService.deploy(createNewAzureNodeDTO.getProviderName(), createNewAzureNodeDTO.getRegion(), createNewAzureNodeDTO.getCpu(), createNewAzureNodeDTO.getMemory());
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
            StorageResource storageResource = getStorageResource(webClientToAzureNode, pnaInitToken);

            // update azure node
            AzureNode azureNodeToBeUpdated = azureNode.get();

            // complete dynamically obtained data from node
            azureNodeToBeUpdated.getNode().setCpuResource(cpuResource);
            azureNodeToBeUpdated.getNode().setMemoryResource(memoryResource);
            azureNodeToBeUpdated.getNode().setStorageResource(storageResource);

            azureNodeToBeUpdated.getNodeMetaData().setRemoteNodeUUID(cloudRegistration.getNodeUUID());
            azureNodeToBeUpdated.getNodeMetaData().setPnaUUID(cloudRegistration.getPnaUUID());
            azureNodeToBeUpdated.getNodeMetaData().setHostname(azureDeloymentResult.getFqdn());
            azureNodeToBeUpdated.setCloudRegistration(cloudRegistration);
            azureNodeToBeUpdated.setAzureDeloymentResult(azureDeloymentResult);

            AzureNode finalAzureNode = this.azureNodeRepository.save(azureNodeToBeUpdated);


            // application dummy
            WebClient webClientToPSM = WebClient.create(this.psmEndpoint);
            CreateNewApplicationDTO pulceoNodeAgentDTO = CreateNewApplicationDTO.builder()
                    .nodeId(azureNodeToBeUpdated.getUuid().toString())
                    .name("pulceo-node-agent")
                    .build();
            webClientToPSM.post()
                    .uri("/api/v1/applications")
                    .header("Content-Type", "application/json")
                    .bodyValue(pulceoNodeAgentDTO)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(e -> {
                        throw new RuntimeException(new NodeServiceException("Failed to create application"));
                    })
                    .block();

            CreateNewApplicationDTO traefikDTO = CreateNewApplicationDTO.builder()
                    .nodeId(azureNodeToBeUpdated.getUuid().toString())
                    .name("traefik")
                    .build();

            webClientToPSM.post()
                    .uri("/api/v1/applications")
                    .header("Content-Type", "application/json")
                    .bodyValue(traefikDTO)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(e -> {
                        throw new RuntimeException(new NodeServiceException("Failed to create application"));
                    })
                    .block();

            PulceoEvent pulceoEvent = PulceoEvent.builder()
                    .eventType(EventType.NODE_CREATED)
                    .payload(azureNodeToBeUpdated.toString())
                    .build();
            this.eventHandler.handleEvent(pulceoEvent);

            return CompletableFuture.completedFuture(finalAzureNode);
        } catch (AzureDeploymentServiceException e) {
            throw new NodeServiceException("Could not create azure node!", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStateByRegion(String region) {
        if (region.equals("eastus")) {
            return "Virginia";
        } else if (region.equals("westeurope")) {
            return "Noord-Holland";
        } else if (region.equals("northeurope")) {
            return "Leinster";
        } else if (region.equals("germanywestcentral")) {
            return "Hessen";
        } else if (region.equals("francecentral")) {
            return "Paris (chef-lieu)";
        } else if (region.equals("eastus2")) {
            return "Virginia";
        } else {
            return "";
        }
    }


    private String getCityByRegion(String region) {
        if (region.equals("eastus")) {
            return "Boydton";
        } else if (region.equals("westeurope")) {
            return "Schiphol";
        } else if (region.equals("northeurope")) {
            return "Dublin";
        } else if (region.equals("germanywestcentral")) {
            return "Frankfurt";
        } else if (region.equals("francecentral")) {
            return "Paris";
        } else if (region.equals("eastus2")) {
            return "Boydton";
        } else {
            return "";
        }
    }

    private String getCountryByRegion(String region) {
        if (region.equals("eastus")) {
            return "USA";
        } else if (region.equals("westeurope")) {
            return "Netherlands";
        } else if (region.equals("northeurope")) {
            return "Ireland";
        } else if (region.equals("germanywestcentral")) {
            return "Germany";
        } else if (region.equals("francecentral")) {
            return "France";
        } else if (region.equals("eastus2")) {
            return "USA";
        } else {
            return "";
        }
    }

    public Node updateNode(UUID nodeUUID, String key, String value) throws NodeServiceException, InterruptedException {
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(nodeUUID);
        if (abstractNode.isEmpty()) {
            throw new NodeServiceException("Node with UUID " + nodeUUID + " does not exist!");
        }
        Node node = abstractNode.get().getNode();
        switch (key) {
            case "name":
                if (this.nameAlreadyExists(value)) {
                    throw new NodeServiceException("Name already exists!");
                }
                node.setName(value);
                break;
            case "type":
                node.setType(NodeType.valueOf(value.toUpperCase()));
                break;
            case "layer":
                node.setLayer(Integer.parseInt(value));
                break;
            case "role":
                node.setRole(NodeRole.valueOf(value.toUpperCase()));
                break;
            case "nodeGroup":
                node.setNodeGroup(value);
                break;
            case "nodeLocationCountry":
                node.setCountry(value);
                break;
            case "nodeLocationState":
                node.setState(value);
                break;
            case "nodeLocationCity":
                node.setCity(value);
                break;
            case "nodeLocationLongitude":
                node.setLongitude(Double.parseDouble(value));
                break;
            case "nodeLocationLatitude":
                node.setLatitude(Double.parseDouble(value));
                break;
            default:
                throw new NodeServiceException("Key not supported!");
        }

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.NODE_UPDATED)
                .payload(node.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        return this.nodeRepository.save(node);
    }

    public CPUResource updateCPUResource(UUID nodeUUID, String key, float value, ResourceType resourceType) throws NodeServiceException, InterruptedException {

        if (value < 0.0f) {
            throw new NodeServiceException("Value must be greater or equals 0!");
        }

        CPUResource cpuResource = this.readCPUResourceByUUID(nodeUUID);

        CPU cpu ;

        if (resourceType == ResourceType.ALLOCATABLE) {
            cpu = cpuResource.getCpuAllocatable();
        } else {
            cpu = cpuResource.getCpuCapacity();
        }

        switch (key) {
            case "cores":
                cpu.setCores((int) value);
                break;
            case "threads":
                cpu.setThreads((int) value);
                break;
            case "bogoMIPS":
                cpu.setBogoMIPS((int) value);
                break;
            case "minimalFrequency":
                cpu.setMinimalFrequency(value);
                break;
            case "averageFrequency":
                cpu.setAverageFrequency(value);
                break;
            case "maximalFrequency":
                cpu.setMaximalFrequency(value);
                break;
            case "shares":
                cpu.setShares((int) value);
                break;
            case "slots":
                cpu.setSlots(value);
                break;
            case "mips":
                cpu.setMIPS(value);
                break;
            case "gflop":
                cpu.setGFlop(value);
                break;
            default:
                throw new NodeServiceException("Key not supported!");
        }

        NodeResourceUpdateRequest nodeResourceUpdateRequest = NodeResourceUpdateRequest.builder()
                .nodeUUID(nodeUUID)
                .key(key)
                .value(value)
                .resourceType(resourceType)
                .build();

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.NODE_CPU_RESOURCES_UPDATED)
                .payload(nodeResourceUpdateRequest.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        return this.cpuResourcesRepository.save(cpuResource);
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

    public MemoryResource updateMemoryResource(UUID uuid, String key, float value, ResourceType resourceType) throws NodeServiceException, InterruptedException {
        if (value < 0.0f) {
            throw new NodeServiceException("Value must be greater or equals 0!");
        }

        MemoryResource memoryResource = this.readMemoryResourceByUUID(uuid);

        Memory memory;

        if (resourceType == ResourceType.ALLOCATABLE) {
            memory = memoryResource.getMemoryAllocatable();
        } else {
            memory = memoryResource.getMemoryCapacity();
        }

        switch (key) {
            case "size":
                memory.setSize(value);
                break;
            case "slots":
                memory.setSlots((int) value);
                break;
            default:
                throw new NodeServiceException("Key not supported!");
        }

        NodeResourceUpdateRequest nodeResourceUpdateRequest = NodeResourceUpdateRequest.builder()
                .nodeUUID(uuid)
                .key(key)
                .value(value)
                .resourceType(resourceType)
                .build();

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.NODE_MEMORY_RESOURCES_UPDATED)
                .payload(nodeResourceUpdateRequest.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        return this.memoryResourcesRepository.save(memoryResource);
    }

    public StorageResource updateStorageResource(UUID uuid, String key, float value, ResourceType resourceType) throws NodeServiceException, InterruptedException {
        if (value < 0.0f) {
            throw new NodeServiceException("Value must be greater or equals 0!");
        }

        StorageResource storageResource = this.readStorageResourceByUUID(uuid);

        Storage storage;

        if (resourceType == ResourceType.ALLOCATABLE) {
            storage = storageResource.getStorageAllocatable();
        } else {
            storage = storageResource.getStorageCapacity();
        }

        switch (key) {
            case "size":
                storage.setSize(value);
                break;
            case "slots":
                storage.setSlots((int) value);
                break;
            default:
                throw new NodeServiceException("Key not supported!");
        }

        NodeResourceUpdateRequest nodeResourceUpdateRequest = NodeResourceUpdateRequest.builder()
                .nodeUUID(uuid)
                .key(key)
                .value(value)
                .resourceType(resourceType)
                .build();

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.NODE_STORAGE_RESOURCES_UPDATED)
                .payload(nodeResourceUpdateRequest.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        return this.storageResourcesRepositoy.save(storageResource);
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

    private StorageResource getStorageResource(WebClient webClient, String pnaInitToken) {
        StorageResourceDTO storageResourceDTO = webClient.get()
                .uri("/api/v1/nodes/localNode/storage")
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + pnaInitToken)
                .retrieve()
                .bodyToMono(StorageResourceDTO.class)
                .onErrorResume(e -> {
                    throw new RuntimeException(new NodeServiceException("Failed to read Storage resources"));
                })
                .block();
        return this.modelMapper.map(storageResourceDTO, StorageResource.class);
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
        } else if (internalNodeType == InternalNodeType.AZURE) {
            AzureNode azureNode = (AzureNode) abstractNode.get();
            Long id = azureNode.getNode().getCpuResource().getId();
            return this.cpuResourcesRepository.findById(id).orElseThrow();

        }
        throw new NodeServiceException("Node type not yet supported!");
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
        } else if (internalNodeType == InternalNodeType.AZURE) {
            AzureNode azureNode = (AzureNode) abstractNode.get();
            Long id = azureNode.getNode().getCpuResource().getId();
            return this.memoryResourcesRepository.findById(id).orElseThrow();
        }
        throw new NodeServiceException("Node type not yet supported!");
    }

    public StorageResource readStorageResourceByUUID(UUID nodeUUID) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(nodeUUID);
        if (abstractNode.isEmpty()) {
            throw new NodeServiceException("Node with UUID " + nodeUUID + " does not exist!");
        }
        InternalNodeType internalNodeType = abstractNode.get().getInternalNodeType();
        if (internalNodeType == InternalNodeType.ONPREM) {
            OnPremNode onPremNode = (OnPremNode) abstractNode.get();
            Long id = onPremNode.getNode().getCpuResource().getId();
            return this.storageResourcesRepositoy.findById(id).orElseThrow();
        } else if (internalNodeType == InternalNodeType.AZURE) {
            AzureNode azureNode = (AzureNode) abstractNode.get();
            Long id = azureNode.getNode().getCpuResource().getId();
            return this.storageResourcesRepositoy.findById(id).orElseThrow();
        }
        throw new NodeServiceException("Node type not yet supported!");

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

    public Optional<AbstractNode> readAbstractNodeById(Long id) {
        return this.abstractNodeRepository.findById(id);
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

    public Optional<AzureNode> readAzureNodeByUUID(UUID nodeUUID) {
        return this.azureNodeRepository.readAzureNodeByUuid(nodeUUID);
    }

    @Async
    public CompletableFuture<Void> deleteNodeByUUID(UUID uuid) throws LinkServiceException, InterruptedException {
        // TODO: findall all links and delete them
        Optional<AbstractNode> abstractNode = this.abstractNodeRepository.findByUuid(uuid);
        if (abstractNode.isEmpty()) {
            throw new LinkServiceException("Node with UUID " + uuid + " does not exist!");
        }

        // find all links where the node is associated with, src and dest
        List<AbstractLink> links = this.linkService.readLinksSrcAndDestByNodeUUID(uuid);

        // delete node metricrequests
        for (AbstractLink link : links) {
            this.linkService.deleteLinkByUUID(link.getUuid());
        }
        WebClient webClient = WebClient.create(this.pmsEndpoint);
        List<ShortMetricResponseDTO> shortMetricResponseDTO = webClient
                .get()
                .uri("/api/v1/metric-requests?linkUUID=" + uuid)
                .retrieve()
                .bodyToFlux(ShortMetricResponseDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Can not delete metric request!"));
                })
                .collectList()
                .block();

        for (ShortMetricResponseDTO metricRequest : shortMetricResponseDTO) {
            webClient.delete()
                    .uri("/api/v1/metric-requests/" + metricRequest.getUuid())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(error -> {
                        throw new RuntimeException(new LinkServiceException("Can not delete metric request!"));
                    })
                    .block();
        }

        // TODO: decide for onprem and azure
        if (abstractNode.get().getInternalNodeType() == InternalNodeType.AZURE) {
            AzureNode azureNode = this.readAzureNode(abstractNode.get().getId());
            this.azureDeploymentService.deleteAzureVirtualMachine(azureNode.getAzureDeloymentResult().getResourceGroupName(), azureNode.getAzureProvider().getProviderMetaData().getProviderName(), false);
        }

        // TODO: check for applications
        this.abstractNodeRepository.delete(abstractNode.get());
        System.out.println("Deleted node with UUID " + uuid);
        return CompletableFuture.completedFuture(null);
    }

}
