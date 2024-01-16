package dev.pulceo.prm.model.node;

import jakarta.validation.constraints.NotBlank;

public class NodeMetaData {
    @NotBlank(message= "PNA id is required!")
    private String pnaUUID;
    @NotBlank(message="Node hostname is required!")
    private String hostname;

}
