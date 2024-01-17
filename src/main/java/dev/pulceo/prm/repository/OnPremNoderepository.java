package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.OnPremNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnPremNoderepository extends CrudRepository<OnPremNode, Long> {
}
