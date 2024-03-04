package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeRole;
import dev.pulceo.prm.model.node.NodeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class NodePropertiesDTO {

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
    private String nodeLocationState = "";

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


    public static NodePropertiesDTO fromNode(Node node) {
        return NodePropertiesDTO.builder()
            .name(node.getName())
            .type(node.getType())
            .layer(node.getLayer())
            .role(node.getRole())
            .nodeLocationCountry(node.getNodeLocationCountry())
            .nodeLocationState(node.getNodeLocationState())
            .nodeLocationCity(node.getNodeLocationCity())
            .nodeLocationLongitude(node.getNodeLocationLongitude())
            .nodeLocationLatitude(node.getNodeLocationLatitude())
            .build();
    }

}
