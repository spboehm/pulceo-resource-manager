package dev.pulceo.prm.service;

import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private final AbstractLinkRepository abstractLinkRepository;
    private final NodeService nodeService;

    @Autowired
    public LinkService(AbstractLinkRepository abstractLinkRepository, NodeService nodeService) {
        this.abstractLinkRepository = abstractLinkRepository;
        this.nodeService = nodeService;
    }

    public NodeLink createNodeLink(NodeLink nodeLink) {
        // TODO: check if nodeLink already exists from srcNodeUUID to destNodeUUID

        // TODO: check if first node exists

        // TODO: check if second node exists

        // TODO: fail-fast otherwise

        // * Transaction begin *

        // TODO: inform srcNode of new Link with CreateNewLinkDTO
        // WebClient webClient = WebClient.create("http://" + hostName + ":7676");

        // TODO: inform destNode of new Link with CreateNewLinkDTO

        // * Transaction end *

        // else, rollback eventually

        // TODO: perform duplicate check
        return this.abstractLinkRepository.save(nodeLink);
    }

}
