package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.NodeTag;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NodeTagRepository extends CrudRepository<NodeTag, Long> {

    Optional<NodeTag> findNodeTagByAbstractNodeAndKey(AbstractNode abstractNode, String key);

    Iterable<NodeTag> findNodeTagsByKeyAndValue(String key, String value);

    Iterable<NodeTag> findNodeTagByKey(String key);

    Iterable<NodeTag> findNodeTagsByValue(String value);
}
