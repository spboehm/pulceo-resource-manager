package dev.pulceo.prm.dto.node;

import dev.pulceo.prm.model.node.OnPremNode;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
@Schema(
        oneOf = {
                NodeDTO.class,
        }
)
public abstract class AbstractNodeDTO {

}
