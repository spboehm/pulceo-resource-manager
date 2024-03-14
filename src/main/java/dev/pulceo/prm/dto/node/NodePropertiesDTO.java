package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeRole;
import dev.pulceo.prm.model.node.NodeTag;
import dev.pulceo.prm.model.node.NodeType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "node_group")
    private String group = "";

    @Builder.Default
    @NotNull
    private String country = "";

    @Builder.Default
    @NotNull
    private String state = "";

    @Builder.Default
    @NotNull
    private String city = "";

    @Builder.Default
    @Min(-180)
    @Max(180)
    private double longitude = 0.000000;

    @Builder.Default
    @Min(-90)
    @Max(90)
    private double latitude = 0.000000;

    @Builder.Default
    List<NodeTagDTO> tags = new ArrayList<>();


    public static NodePropertiesDTO fromNode(Node node) {
        return NodePropertiesDTO.builder()
            .name(node.getName())
            .type(node.getType())
            .layer(node.getLayer())
            .role(node.getRole())
            .group(node.getNodeGroup())
            .country(node.getCountry())
            .state(node.getState())
            .city(node.getCity())
            .longitude(node.getLongitude())
            .latitude(node.getLatitude())
            .tags(node.getNodeTags().stream().map(NodeTagDTO::fromNodeTag).toList())
            .build();
    }

}
