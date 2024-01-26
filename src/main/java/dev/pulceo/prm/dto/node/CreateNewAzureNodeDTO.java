package dev.pulceo.prm.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AzureNode", description = "Create a new node on Azure.")
public class CreateNewAzureNodeDTO extends CreateNewAbstractNodeDTO {

    private String providerName;
    private VMSkuType vmSkuType;

}
