package dev.pulceo.prm.dto.provider.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewOnPremNodeDTO {

    private String providerName;
    private String hostname;
    private String pnaInitToken;

}