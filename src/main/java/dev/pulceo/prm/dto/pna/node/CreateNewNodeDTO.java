package dev.pulceo.prm.dto.pna.node;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewNodeDTO {

    @NotBlank(message= "PNA id is required!")
    private String pnaUUID;

    @Builder.Default
    private boolean isLocalNode = false;

    @NotBlank(message="Name is required!")
    private String name;

    @Builder.Default
    @NotNull(message = "Node type is required!")
    private NodeType type = NodeType.EDGE;

    @Builder.Default
    @Min(1)
    @Max(16)
    private int layer = 1;

    @Builder.Default
    @NotNull(message="Node role is required!")
    private NodeRole role = NodeRole.WORKLOAD;

    @Builder.Default
    @NotNull
    private String nodeLocationCountry = "";

    @Builder.Default
    @NotNull
    private String nodeLocationCity = "";

    @Builder.Default
    @Min(-180)
    @Max(180)
    private double nodeLocationLongitude = 0.000000;

    @Builder.Default
    @Min(-90)
    @Max(90)
    private double nodeLocationLatitude = 0.000000;

    @NotBlank(message="Node endpoint is required!")
    @URL
    private String pnaEndpoint;

    @NotBlank(message="Node hostname is required!")
    private String host;

}
