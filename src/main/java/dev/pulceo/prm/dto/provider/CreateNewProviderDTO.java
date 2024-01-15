package dev.pulceo.prm.dto.provider;

import dev.pulceo.prm.model.provider.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewProviderDTO {

    private String providerName;
    private ProviderType providerType;

}
