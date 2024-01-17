package dev.pulceo.prm.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudRegistrationRequestDTO {

    private String prmUUID;
    private String prmEndpoint;
    private String pnaInitToken;

}
