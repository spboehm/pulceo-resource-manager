package dev.pulceo.prm.service;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

    private final NodeRepository nodeRepository;

    @Autowired
    public NodeService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public Node createNode(Node node) {
        return this.nodeRepository.save(node);
    }

}
