package dev.pulceo.prm.dto.resource;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ResourceInformationDTO {

    private String resourceType;
    private String resourceUUID;
    private String resourceId;
    private String url;

}
