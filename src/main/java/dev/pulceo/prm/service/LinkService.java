package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.link.NodeLinkDTO;
import dev.pulceo.prm.dto.pna.node.CreateNewNodeDTO;
import dev.pulceo.prm.dto.pna.node.CreateNewNodeLinkOnPNADTO;
import dev.pulceo.prm.dto.pna.node.NodeDTO;
import dev.pulceo.prm.dto.psm.ShortMetricResponseDTO;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.internal.G6.model.G6Edge;
import dev.pulceo.prm.model.event.EventType;
import dev.pulceo.prm.model.event.PulceoEvent;
import dev.pulceo.prm.model.link.AbstractLink;
import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.NodeLinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class LinkService {

    private final AbstractLinkRepository abstractLinkRepository;
    private final NodeLinkRepository nodeLinkRepository;
    private final NodeService nodeService;

    @Value("${webclient.scheme}")
    private String webClientScheme;

    @Value("${pms.endpoint}")
    private String pmsEndpoint;

    private final EventHandler eventHandler;

    @Autowired
    public LinkService(AbstractLinkRepository abstractLinkRepository, NodeService nodeService, NodeLinkRepository nodeLinkRepository, EventHandler eventHandler) {
        this.abstractLinkRepository = abstractLinkRepository;
        this.nodeService = nodeService;
        this.nodeLinkRepository = nodeLinkRepository;
        this.eventHandler = eventHandler;
    }

    // TODO: ambigious
    @Transactional
    public NodeLink createNodeLinkById(String name, String srcNodeId, String destNodeId) throws Exception {

        Optional<AbstractLink> abstractNodeLink = this.readLinkByName(name);
        if (abstractNodeLink.isPresent()) {
            throw new LinkServiceException("Link with name %s already exists!".formatted(name));
        }

        Optional<AbstractNode> srcNode = this.resolveAbstractNode(srcNodeId);
        if (srcNode.isEmpty()) {
            throw new LinkServiceException("Source node with id %s does not exist!".formatted(srcNodeId));
        }

        Optional<AbstractNode> destNode = this.resolveAbstractNode(destNodeId);
        if (destNode.isEmpty()) {
            throw new LinkServiceException("Destination node with id %s does not exist!".formatted(destNodeId));
        }
        
        Optional<NodeLink> existingNodeLink = this.nodeLinkRepository.findBySrcNodeAndDestNode(srcNode.get(), destNode.get());
        if (existingNodeLink.isPresent()) {
            throw new LinkServiceException("Link from %s to %s already exists!".formatted(srcNodeId, destNodeId));
        }

        // TODO: exception handling
        NodeLink nodeLink = NodeLink.builder()
                .name(name)
                .srcNode(srcNode.get())
                .destNode(destNode.get())
                .build();

        return this.createNodeLink(nodeLink);
    }

    @Transactional
    // TODO: replace with name and srcNodeUUID and destNodeUUID or do by request object
    public NodeLink createNodeLink(NodeLink nodeLink) throws LinkServiceException, InterruptedException {
        /* TODO: check if nodeLink already exists from srcNodeUUID to destNodeUUID */

        // check if link does already exist
        Optional<NodeLink> readNodeLink = this.readNodeLinkByUUID(nodeLink.getUuid());
        if (readNodeLink.isPresent()) {
            return readNodeLink.get();
        }

        // first node available
        Optional<AbstractNode> abstractSrcNode = this.nodeService.readAbstractNodeByUUID(nodeLink.getSrcNode().getUuid());
        if (abstractSrcNode.isEmpty()) {
            throw new LinkServiceException("Source node with id %s does not exist!".formatted(nodeLink.getSrcNode().getUuid()));
        }

        // second node available
        Optional<AbstractNode> abstractDestNode = this.nodeService.readAbstractNodeByUUID(nodeLink.getDestNode().getUuid());
        if (abstractDestNode.isEmpty()) {
            throw new LinkServiceException("Destination node with id %s does not exist!".formatted(nodeLink.getDestNode().getUuid()));
        }

        // the remote UUID in perspective of the srcNode (localnode)
        UUID srcRemoteNodeUUID = abstractSrcNode.get().getNodeMetaData().getRemoteNodeUUID();

        // TODO: replace pnaEndpoint with https
        // create a new logical node inside the srcNode
        CreateNewNodeDTO createDestNewNodeDTO = CreateNewNodeDTO.builder()
                .name(abstractDestNode.get().getNodeMetaData().getHostname())
                .pnaUUID(String.valueOf(abstractDestNode.get().getNodeMetaData().getPnaUUID()))
                .pnaEndpoint(this.webClientScheme + "://" + abstractDestNode.get().getNodeMetaData().getHostname() + ":7676")
                .host(abstractDestNode.get().getNodeMetaData().getHostname())
                .build();

        WebClient wcNodeDTO = WebClient.create(this.webClientScheme + "://" + abstractSrcNode.get().getNodeMetaData().getHostname() + ":7676");
        NodeDTO destNodeDTO = wcNodeDTO.post()
                .uri("/api/v1/nodes")
                .header("Authorization", "Basic " + abstractSrcNode.get().getToken())
                .bodyValue(createDestNewNodeDTO)
                .retrieve()
                .bodyToMono(NodeDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Error while creating logical node on dest Node"));
                })
                .block();

        // create new node link DTO for the srcNode, this information is effectively used in the srcNode to make the link
        CreateNewNodeLinkOnPNADTO createNewNodeLinkDTO = CreateNewNodeLinkOnPNADTO.builder()
                .name(nodeLink.getName())
                .srcNodeUUID(srcRemoteNodeUUID) // remote srcNodeUUID, is going to be replaced by local node UUID in pna
                .destNodeUUID(destNodeDTO.getNodeUUID()) // destNodeUUID is replaced by the previously return remote node UUID
                .build();

        // srcNode to destNode
        NodeLinkDTO srcNodeLinkDTO = wcNodeDTO.post()
                .uri("/api/v1/links")
                .header("Authorization", "Basic " + abstractSrcNode.get().getToken())
                .bodyValue(createNewNodeLinkDTO)
                .retrieve()
                .bodyToMono(NodeLinkDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Error while creating link from srcNode to destNode"));
                })
                .block();
        // TODO: assertions
        // TODO: consider a bidrectional link
        /* Transaction ends */
        // before persisting the NodeLink we need to set the remoteNodeLinkUUID to the UUID of the link pna returns
        nodeLink.setRemoteNodeLinkUUID(UUID.fromString(srcNodeLinkDTO.getLinkUUID()));

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.LINK_CREATED)
                .payload(nodeLink.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        // TODO: assertions
        return this.abstractLinkRepository.save(nodeLink);
    }

    // TODO: types are unclear
    public Optional<AbstractLink> readLinkByUUID(UUID uuid) {
        return this.abstractLinkRepository.findByUuid(uuid);
    }

    // TODO: types are unclear
    public Optional<NodeLink> readNodeLinkByUUID(UUID uuid) {
        return this.abstractLinkRepository.findNodeLinkByUuid(uuid);
    }

    // TODO: types are unclear
    public List<AbstractLink> readAllLinks() {
        List<AbstractLink> list = new ArrayList<>();
        this.abstractLinkRepository.findAll().forEach(list::add);
        return list;
    }

    public List<G6Edge> readG6EdgeData() {
        Iterable<NodeLink> nodeLinks = this.nodeLinkRepository.findAll();
        List<G6Edge> list = new ArrayList<>();
        nodeLinks.forEach(nodeLink -> {
            list.add(nodeLink.getG6Edge());
        });
        return list;
    }

    // TODO: move to nodeSvc
    private Optional<AbstractNode> resolveAbstractNode(String id) {
        Optional<AbstractNode> abstractNode;
        // TODO: add resolve to name here, heck if UUID
        if (checkIfUUID(id)) {
            abstractNode = this.nodeService.readAbstractNodeByUUID(UUID.fromString(id));
        } else {
            abstractNode = this.nodeService.readAbstractNodeByName(id);
        }
        return abstractNode;
    }

    // TODO: move to nodeSvc
    private static boolean checkIfUUID(String uuid)  {
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidRegex);
    }

    public Optional<AbstractLink> readLinkByName(String id) {
        return this.abstractLinkRepository.findByName(id);
    }

    @Transactional
    public void deleteLinkByUUID(UUID linkUuid) throws LinkServiceException, InterruptedException {

        // TODO: retrieve metric-requests from psm and delete them
        Optional<AbstractLink> abstractLink = this.readLinkByUUID(linkUuid);
        if (abstractLink.isEmpty()) {
            throw new LinkServiceException("Link with UUID " + linkUuid + " does not exist!");
        }

        WebClient webClient = WebClient.create(this.pmsEndpoint);
        List<ShortMetricResponseDTO> metricRequests = webClient.get()
                .uri("/api/v1/metric-requests?linkUUID=" + linkUuid)
                .retrieve()
                .bodyToFlux(ShortMetricResponseDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Can not delete metric request!"));
                })
                .collectList()
                .block();

        // delete all metric requests
        for (ShortMetricResponseDTO metricRequest : metricRequests) {
            webClient.delete()
                    .uri("/api/v1/metric-requests/" + metricRequest.getUuid())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(error -> {
                        throw new RuntimeException(new LinkServiceException("Can not delete metric request!"));
                    })
                    .block();
        }

        // delete link
        NodeLink nodeLink = (NodeLink) abstractLink.get();
        AbstractNode srcNode = nodeLink.getSrcNode();

        WebClient webClientToDestNode = WebClient.create(this.webClientScheme + "://" + srcNode.getNodeMetaData().getHostname() + ":7676");
        webClientToDestNode.delete()
                .uri("/api/v1/links/" + nodeLink.getRemoteNodeLinkUUID())
                .header("Authorization", "Basic " + srcNode.getToken())
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Can not delete link!"));
                })
                .block();

        PulceoEvent pulceoEvent = PulceoEvent.builder()
                .eventType(EventType.LINK_DELETED)
                .payload(nodeLink.toString())
                .build();
        this.eventHandler.handleEvent(pulceoEvent);

        this.abstractLinkRepository.delete(abstractLink.get());
    }

    public List<AbstractLink> readLinksSrcAndDestByNodeUUID(UUID uuid) {
        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return new ArrayList<>();
        }
        List<AbstractLink> abstractLinks = new ArrayList<>();
        this.nodeLinkRepository.findBySrcNode(abstractNode.get()).forEach(abstractLinks::add);
        this.nodeLinkRepository.findByDestNode(abstractNode.get()).forEach(abstractLinks::add);
        return abstractLinks;
    }
}
