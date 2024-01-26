package dev.pulceo.prm.dto.provider.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nodeType",
        use = JsonTypeInfo.Id.NAME,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateNewOnPremNodeDTO.class, name = "ONPREM"),
        @JsonSubTypes.Type(value = CreateNewAzureNodeDTO.class, name = "AZURE")
})
public abstract class CreateNewAbstractNodeDTO {
    private NodeDTOType nodeType;
}
