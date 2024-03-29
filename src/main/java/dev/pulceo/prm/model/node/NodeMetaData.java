package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.UUID;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class NodeMetaData extends BaseEntity {
    @NotNull(message= "Remote node id is required!")
    @Builder.Default
    private UUID remoteNodeUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @NotNull(message= "PNA id is required!")
    @Builder.Default
    private UUID pnaUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    @NotNull(message="Node hostname is required!")
    @Builder.Default
    private String hostname = "PENDING";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeMetaData that = (NodeMetaData) o;

        if (!Objects.equals(remoteNodeUUID, that.remoteNodeUUID))
            return false;
        if (!Objects.equals(pnaUUID, that.pnaUUID)) return false;
        return Objects.equals(hostname, that.hostname);
    }

    @Override
    public int hashCode() {
        int result = remoteNodeUUID != null ? remoteNodeUUID.hashCode() : 0;
        result = 31 * result + (pnaUUID != null ? pnaUUID.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NodeMetaData{" +
                "remoteNodeUUID=" + remoteNodeUUID +
                ", pnaUUID=" + pnaUUID +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
