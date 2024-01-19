package dev.pulceo.prm.model.registration;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class CloudRegistration extends BaseEntity {

    private UUID pnaUUID;
    private UUID prmUUID;
    private String prmEndpoint;
    private String pnaToken;

}
