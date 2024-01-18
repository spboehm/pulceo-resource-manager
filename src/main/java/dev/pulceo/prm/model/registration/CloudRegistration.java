package dev.pulceo.prm.model.registration;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class CloudRegistration extends BaseEntity {

    private String pnaUUID;
    private String prmUUID;
    private String prmEndpoint;
    private String pnaToken;

}
