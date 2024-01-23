package dev.pulceo.prm.service;

import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private final AbstractLinkRepository abstractLinkRepository;

    @Autowired
    public LinkService(AbstractLinkRepository abstractLinkRepository) {
        this.abstractLinkRepository = abstractLinkRepository;
    }

    public NodeLink createNodeLink(NodeLink nodeLink) {
        // TODO: perform duplicate check
        return this.abstractLinkRepository.save(nodeLink);
    }


}
