package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.link.NodeLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeLinkRepository extends CrudRepository<NodeLink, Long> {

}
