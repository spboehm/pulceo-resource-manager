package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.model.node.AbstractNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NodeLinkRepository extends CrudRepository<NodeLink, Long> {

    Iterable<NodeLink> findBySrcNode(AbstractNode abstractNode);

    Iterable<NodeLink> findByDestNode(AbstractNode abstractNode);

    Optional<NodeLink> findBySrcNodeAndDestNode(AbstractNode abstractNode, AbstractNode abstractNode1);
}
