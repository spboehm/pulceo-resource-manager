package dev.pulceo.prm.dto.node;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

@Data
@NoArgsConstructor
@SuperBuilder
public class CreateTagDTO {
    private String resourceId;
    private TagType tagType;
    private String tagKey;
    private String tagValue;
}
