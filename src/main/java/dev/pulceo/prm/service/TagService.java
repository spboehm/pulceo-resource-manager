package dev.pulceo.prm.service;

import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.exception.TagServiceException;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.NodeTag;
import dev.pulceo.prm.repository.NodeTagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final NodeTagRepository nodeTagRepository;
    private final NodeService nodeService;

    @Autowired
    public TagService(NodeTagRepository nodeTagRepository, NodeService nodeService) {
        this.nodeTagRepository = nodeTagRepository;
        this.nodeService = nodeService;
    }

    @Transactional
    public NodeTag createNodeTag(NodeTag nodeTag) throws TagServiceException {
        // first resolve the abstract node and check if it exists
        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(nodeTag.getAbstractNode().getUuid());
        if (abstractNode.isEmpty()) {
            throw new TagServiceException("Node with id %s".formatted(nodeTag.getAbstractNode().getUuid()));
        }

        // if tagKey does already exist, fail the creation and require an update
        Optional<NodeTag> retrievedNodeTag = this.nodeTagRepository.findNodeTagByAbstractNodeAndKey(abstractNode.get(), nodeTag.getKey());
        if (retrievedNodeTag.isPresent()) {
            // fail and require the user to update instead of creating a new one
            throw new TagServiceException("Tag %s already exists for this node...consider updating instead of creating a new one.".formatted(nodeTag.getKey()));
        } else {
            // create the node tag on node
            try {
                nodeTag.setAbstractNode(abstractNode.get());
                nodeTag.setNode(abstractNode.get().getNode());
                this.nodeService.addTagToNode(abstractNode.get().getUuid(), nodeTag);
            } catch (NodeServiceException e) {
                throw new TagServiceException("Failed to add tag to node...");
            }
            return NodeTag.builder()
                    .abstractNode(abstractNode.get())
                    .node(abstractNode.get().getNode())
                    .key(nodeTag.getKey())
                    .value(nodeTag.getValue())
                    .build();
        }
    }

    public List<NodeTag> readNodeTags(Optional<String> key, Optional<String> value) {
        List<NodeTag> nodeTags = new ArrayList<>();
        if (key.isPresent() && value.isPresent()) {
            this.nodeTagRepository.findNodeTagsByKeyAndValue(key.get(), value.get()).forEach(nodeTags::add);
        } else if (key.isPresent()) {
            this.nodeTagRepository.findNodeTagByKey(key.get()).forEach(nodeTags::add);
        } else if (value.isPresent()) {
            this.nodeTagRepository.findNodeTagsByValue(value.get()).forEach(nodeTags::add);
        } else {
            this.nodeTagRepository.findAll().forEach(nodeTags::add);
        }
        return nodeTags;
    }

}
