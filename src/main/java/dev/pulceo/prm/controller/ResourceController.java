package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.pna.node.CPU.CPUResourceDTO;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.CPUResource;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;
    private final NodeService nodeService;

    @Autowired
    public ResourceController(ResourceService resourceService, NodeService nodeService) {
        this.resourceService = resourceService;
        this.nodeService = nodeService;
    }

    @GetMapping("/cpus")
    public ResponseEntity<List<CPUResourceDTO>> readAllCPUs() {
        List<AbstractNode> nodes = this.nodeService.readAllNodes();
        List<CPUResourceDTO> cpuResourcesDTO = new ArrayList<>();
        for (AbstractNode node : nodes) {
            Long cpuResourcesId = node.getNode().getCpuResource().getId();
            Optional<CPUResource> cpuResource = this.resourceService.readCPUResourcesById(cpuResourcesId);
            if (cpuResource.isPresent()) {
                cpuResourcesDTO.add(CPUResourceDTO.fromCPUResource(node.getUuid(), node.getNode().getName(), cpuResource.get()));
            } else {
                return ResponseEntity.status(400).build();
            }
        }
        return ResponseEntity.ok(cpuResourcesDTO);
    }
}
