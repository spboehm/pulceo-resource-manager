package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.link.AbstractLink;
import dev.pulceo.prm.model.link.NodeLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbstractLinkRepository extends CrudRepository<AbstractLink, Long> {

    Optional<NodeLink> findNodeLinkByUuid(UUID uuid);

    Optional<AbstractLink> findByUuid(UUID uuid);

    Optional<AbstractLink> findByName(String id);
}
