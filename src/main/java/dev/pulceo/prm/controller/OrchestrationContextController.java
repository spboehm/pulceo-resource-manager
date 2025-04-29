package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.orchestration.OrchestrationContextDTO;
import dev.pulceo.prm.dto.orchestration.UpdateOrchestrationContextDTO;
import dev.pulceo.prm.model.orchestration.ImmutableOrchestrationContext;
import dev.pulceo.prm.service.LinkService;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.OrchestrationContextService;
import dev.pulceo.prm.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestration-context")
public class OrchestrationContextController {

    private final OrchestrationContextService orchestrationContextService;
    private final ProviderService providerService;
    private final NodeService nodeService;
    private final LinkService linkService;

    @Autowired
    public OrchestrationContextController(OrchestrationContextService orchestrationContextService, ProviderService providerService, NodeService nodeService, LinkService linkService) {
        this.orchestrationContextService = orchestrationContextService;
        this.providerService = providerService;
        this.nodeService = nodeService;
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<OrchestrationContextDTO> readOrchestrationContext() {
        return ResponseEntity.ok(OrchestrationContextDTO.fromOrchestrationContext(orchestrationContextService.getOrchestrationContext()));
    }

    @PutMapping
    public ResponseEntity<OrchestrationContextDTO> updateOrchestrationContext(@RequestBody UpdateOrchestrationContextDTO fromUpdateUpdateOrchestrationContextDTO) {
        ImmutableOrchestrationContext updatedOrchestrationContext = this.orchestrationContextService.updateOrchestrationContext(ImmutableOrchestrationContext.fromUpdateUpdateOrchestrationContextDTO(fromUpdateUpdateOrchestrationContextDTO));
        return ResponseEntity.ok(OrchestrationContextDTO.fromOrchestrationContext(updatedOrchestrationContext));
    }

    @PostMapping("/reset")
    public void deleteOrchestrationContext() {
        this.linkService.reset();
        this.nodeService.reset();
        this.providerService.reset();
    }
}
