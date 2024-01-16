package dev.pulceo.prm.dto.provider.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewOnPremNodeDTO {

    private UUID providerUUID;
    private String hostname;
    private String pnaInitToken;

}
