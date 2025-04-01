package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.AzureNode;
import dev.pulceo.prm.model.node.NodeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AzureNodeRepository extends CrudRepository<AzureNode, Long> {

    @Override
    @EntityGraph(value="graph.AzureNode.azureProvider")
    Optional<AzureNode> findById(Long id);

    @EntityGraph(value="graph.AzureNode.azureProvider")
    Optional<AzureNode> readAzureNodeByUuid(UUID uuid);

    @EntityGraph(value="graph.AzureNode.azureProvider")
    List<AzureNode> findAzureNodesByNodeType(NodeType nodeType);
}
