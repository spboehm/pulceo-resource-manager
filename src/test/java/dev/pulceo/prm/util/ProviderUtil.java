package dev.pulceo.prm.util;

import dev.pulceo.prm.model.provider.Provider;
import dev.pulceo.prm.model.provider.ProviderType;

public class ProviderUtil {

    public static Provider createTestProvider() {
        return Provider.builder()
                .providerName("testProvider")
                .providerType(ProviderType.ON_PREM)
                .build();
    }

}
