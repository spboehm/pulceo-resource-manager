package dev.pulceo.prm.model.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public class NodeResourceUpdateRequest {

    private UUID nodeUUID;
    private String key;
    private float value;
    private ResourceType resourceType;

    @Override
    public String toString() {
        return "ResourceUpdateRequest{" +
                "nodeUUID=" + nodeUUID +
                ", key='" + key + '\'' +
                ", value=" + value +
                ", resourceType=" + resourceType +
                '}';
    }
}
