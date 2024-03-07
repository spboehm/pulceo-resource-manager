package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.StorageResource;
import org.springframework.data.repository.CrudRepository;

public interface StorageResourcesRepositoy extends CrudRepository<StorageResource, Long> {
}
