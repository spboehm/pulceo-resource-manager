package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.OnPremNode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnPremNodeRepository extends CrudRepository<OnPremNode, Long> {

    @Override
    @EntityGraph(value="graph.OnPremNode.onPremProvider")
    Optional<OnPremNode> findById(Long id);
}
