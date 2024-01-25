package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.AbstractNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbstractNodeRepository extends CrudRepository<AbstractNode, Long> {
    Optional<AbstractNode> findByUuid(UUID uuid);
}
