package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.model.node.AbstractNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeLinkRepository extends CrudRepository<NodeLink, Long> {

    Optional<NodeLink> findBySrcNodeAndDestNode(AbstractNode abstractNode, AbstractNode abstractNode1);
}
