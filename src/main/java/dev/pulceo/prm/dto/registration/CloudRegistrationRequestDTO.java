package dev.pulceo.prm.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudRegistrationRequestDTO {

    private UUID prmUUID;
    private String prmEndpoint;
    private String pnaInitToken;

}
