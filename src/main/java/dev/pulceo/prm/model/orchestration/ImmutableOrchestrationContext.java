package dev.pulceo.prm.model.orchestration;


import dev.pulceo.prm.api.dto.orchestration.OrchestrationContextFromPsmDTO;
import dev.pulceo.prm.dto.orchestration.UpdateOrchestrationContextDTO;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ImmutableOrchestrationContext {

    String uuid;
    String name;

    public static ImmutableOrchestrationContext fromOrchestrationContextFromPsmDTO(OrchestrationContextFromPsmDTO orchestrationContextFromPsmDTO) {
        return ImmutableOrchestrationContext.builder()
                .uuid(orchestrationContextFromPsmDTO.getUuid())
                .name(orchestrationContextFromPsmDTO.getName())
                .build();
    }

    public static ImmutableOrchestrationContext fromUpdateUpdateOrchestrationContextDTO(UpdateOrchestrationContextDTO updateOrchestrationContextDTO) {
        return ImmutableOrchestrationContext.builder()
                .uuid(updateOrchestrationContextDTO.getUuid())
                .name(updateOrchestrationContextDTO.getName())
                .build();
    }

}
