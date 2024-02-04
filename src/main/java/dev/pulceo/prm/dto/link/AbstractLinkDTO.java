package dev.pulceo.prm.dto.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractLinkDTO {
    private LinkTypeDTO linkType;
}
