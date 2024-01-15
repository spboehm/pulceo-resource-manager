package dev.pulceo.prm.dtos;

import dev.pulceo.prm.dto.provider.CreateNewProviderDTO;
import dev.pulceo.prm.model.provider.ProviderType;

public class ProviderDTOUtil {

    public static CreateNewProviderDTO createNewProviderDTO() {
        return CreateNewProviderDTO.builder()
                .providerName("testProvider")
                .providerType(ProviderType.ON_PREM)
                .build();
    }

}
