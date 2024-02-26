package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.AzureNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AzureNodeRepository extends CrudRepository<AzureNode, Long> {
    Optional<AzureNode> readAzureNodeByUuid(UUID uuid);
}
