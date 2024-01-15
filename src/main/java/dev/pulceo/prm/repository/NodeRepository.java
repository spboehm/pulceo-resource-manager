package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.Node;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRepository extends CrudRepository<Node, Long> {

}
