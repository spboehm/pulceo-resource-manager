package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.NodeMetaData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NodeMetaDataRepository extends CrudRepository<NodeMetaData, Long> {
    Optional<NodeMetaData> findByHostname(String hostName);
}
