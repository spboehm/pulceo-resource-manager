package dev.pulceo.prm.service;

import dev.pulceo.prm.dto.link.CreateNewNodeLinkDTO;
import dev.pulceo.prm.dto.link.NodeLinkDTO;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.internal.G6.model.G6Edge;
import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.NodeLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkService {

    private final AbstractLinkRepository abstractLinkRepository;
    private final NodeLinkRepository nodeLinkRepository;
    private final NodeService nodeService;

    @Autowired
    public LinkService(AbstractLinkRepository abstractLinkRepository, NodeService nodeService, NodeLinkRepository nodeLinkRepository) {
        this.abstractLinkRepository = abstractLinkRepository;
        this.nodeService = nodeService;
        this.nodeLinkRepository = nodeLinkRepository;
    }

    public NodeLink createNodeLink(NodeLink nodeLink) throws LinkServiceException {
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

        /* Transaction begins */
        CreateNewNodeLinkDTO createNewNodeLinkDTO = CreateNewNodeLinkDTO.builder()
                .name(nodeLink.getName())
                .srcNodeUUID(nodeLink.getSrcNode().getUuid())
                .destNodeUUID(nodeLink.getDestNode().getUuid())
                .build();

        // srcNode to destNode
        WebClient webClient = WebClient.create("http://" + abstractSrcNode.get().getNodeMetaData().getHostname() + ":7676");
        NodeLinkDTO srcNodeLinkDTO = webClient.post()
                .uri("/api/v1/links")
                .bodyValue(createNewNodeLinkDTO)
                .retrieve()
                .bodyToMono(NodeLinkDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Error while creating link from srcNode to destNode"));
                })
                .block();
        // TODO: assertions
        // destNode to srcNode
        webClient = WebClient.create("http://" + abstractDestNode.get().getNodeMetaData().getHostname() + ":7676");
        NodeLinkDTO destNodeLinkDTO = webClient.post()
                .uri("/api/v1/links")
                .bodyValue(createNewNodeLinkDTO)
                .retrieve()
                .bodyToMono(NodeLinkDTO.class)
                .onErrorResume(error -> {
                    throw new RuntimeException(new LinkServiceException("Error while creating link from destNode to srcNode"));
                })
                .block();
        /* Transaction ends */
        // TODO: assertions
        return this.abstractLinkRepository.save(nodeLink);
    }

    public Optional<NodeLink> readNodeLinkByUUID(UUID uuid) {
        return this.abstractLinkRepository.findByUuid(uuid);
    }

    public List<G6Edge> readG6EdgeData() {
        Iterable<NodeLink> nodeLinks = this.nodeLinkRepository.findAll();
        List<G6Edge> list = new ArrayList<>();
        nodeLinks.forEach(nodeLink -> {
            list.add(nodeLink.getG6Edge());
        });
        return list;
    }
}
