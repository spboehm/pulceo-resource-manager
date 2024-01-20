package dev.pulceo.prm.model.registration;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.UUID;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class CloudRegistration extends BaseEntity {

    private UUID pnaUUID;
    private UUID prmUUID;
    private String prmEndpoint;
    private String pnaToken;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudRegistration that = (CloudRegistration) o;

        if (!Objects.equals(pnaUUID, that.pnaUUID)) return false;
        if (!Objects.equals(prmUUID, that.prmUUID)) return false;
        if (!Objects.equals(prmEndpoint, that.prmEndpoint)) return false;
        return Objects.equals(pnaToken, that.pnaToken);
    }

    @Override
    public int hashCode() {
        int result = pnaUUID != null ? pnaUUID.hashCode() : 0;
        result = 31 * result + (prmUUID != null ? prmUUID.hashCode() : 0);
        result = 31 * result + (prmEndpoint != null ? prmEndpoint.hashCode() : 0);
        result = 31 * result + (pnaToken != null ? pnaToken.hashCode() : 0);
        return result;
    }
}
