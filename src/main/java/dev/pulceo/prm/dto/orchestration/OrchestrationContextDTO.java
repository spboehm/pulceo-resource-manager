package dev.pulceo.prm.dto.orchestration;

import dev.pulceo.prm.model.orchestration.ImmutableOrchestrationContext;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@Getter
@Setter
@NoArgsConstructor
public class OrchestrationContextDTO {

    @Builder.Default
    private String service = "prm";
    private String uuid;
    private String name;

    public static OrchestrationContextDTO fromOrchestrationContext(ImmutableOrchestrationContext orchestrationContext) {
        return OrchestrationContextDTO.builder()
                .uuid(orchestrationContext.getUuid())
                .name(orchestrationContext.getName())
                .build();
    }
}
