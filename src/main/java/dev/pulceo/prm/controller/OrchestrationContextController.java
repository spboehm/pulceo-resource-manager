package dev.pulceo.prm.controller;

import dev.pulceo.prm.service.LinkService;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.ProviderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrchestrationContextController {

    private final ProviderService providerService;
    private final NodeService nodeService;
    private final LinkService linkService;

    public OrchestrationContextController(ProviderService providerService, NodeService nodeService, LinkService linkService) {
        this.providerService = providerService;
        this.nodeService = nodeService;
        this.linkService = linkService;
    }

    @PostMapping("/api/v1/orchestration-context/reset")
    public void deleteOrchestrationContext() {
        this.linkService.reset();
        this.nodeService.reset();
        this.providerService.reset();
    }
}
