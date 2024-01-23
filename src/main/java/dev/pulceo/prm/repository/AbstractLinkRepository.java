package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.link.AbstractLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractLinkRepository extends CrudRepository<AbstractLink, Long> {

}
