package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends CrudRepository<Node, Long> {

    @Override
    @EntityGraph(value = "graph.Node.nodeTags", attributePaths = {"tags"})
    Optional<Node> findById(Long id);

    List<Node> findNodesByType(NodeType nodeType);

}
