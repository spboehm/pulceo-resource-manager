package dev.pulceo.prm.dto.psm;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateNewApplicationDTO {

    @Builder.Default
    private String nodeId = "00000000-0000-0000-0000-000000000000";
    @Builder.Default
    private String name = "";
    @Builder.Default
    private List<CreateNewApplicationComponentDTO> applicationComponents = new ArrayList<>();

}
