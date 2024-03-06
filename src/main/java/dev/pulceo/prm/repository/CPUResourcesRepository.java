package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.CPUResource;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CPUResourcesRepository extends CrudRepository<CPUResource, Long> {

}
