package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.dto.pna.node.storage.StorageResourceDTO;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<CPUResourceDTO>> readAllCPUs(@RequestParam(required = false) NodeType type) throws NodeServiceException {
        List<AbstractNode> nodes;
        if (type == null) {
            nodes = this.nodeService.readAllNodes();
        } else {
            nodes = this.nodeService.readNodesByType(type);
            if (nodes.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
        }
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

    @GetMapping("/memory")
    public ResponseEntity<List<MemoryResourceDTO>> readAllMemory(@RequestParam(required = false) NodeType type) throws NodeServiceException {
        List<AbstractNode> nodes;
        if (type == null) {
            nodes = this.nodeService.readAllNodes();
        } else {
            nodes = this.nodeService.readNodesByType(type);
            if (nodes.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
        }
        List<MemoryResourceDTO> memoryResourceDTO = new ArrayList<>();
        for (AbstractNode node : nodes) {
            Long memoryResourcesID = node.getNode().getMemoryResource().getId();
            Optional<MemoryResource> memoryResource = this.resourceService.readMemoryResourcesById(memoryResourcesID);
            if (memoryResource.isPresent()) {
                memoryResourceDTO.add(MemoryResourceDTO.fromMemoryResource(node.getUuid(), node.getNode().getName(), memoryResource.get()));
            } else {
                return ResponseEntity.status(400).build();
            }
        }
        return ResponseEntity.ok(memoryResourceDTO);
    }

    @GetMapping("/storage")
    public ResponseEntity<List<StorageResourceDTO>> readAllStorage(@RequestParam(required = false) NodeType type) throws NodeServiceException {
        List<AbstractNode> nodes;
        if (type == null) {
            nodes = this.nodeService.readAllNodes();
        } else {
            nodes = this.nodeService.readNodesByType(type);
            if (nodes.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
        }
        List<StorageResourceDTO> storageResourceDTO = new ArrayList<>();
        for (AbstractNode node : nodes) {
            Long storageResourcesID = node.getNode().getStorageResource().getId();
            Optional<StorageResource> storageResource = this.resourceService.readStorageResourcesById(storageResourcesID);
            if (storageResource.isPresent()) {
                storageResourceDTO.add(StorageResourceDTO.fromStorageResource(node.getUuid(), node.getNode().getName(), storageResource.get()));
            } else {
                return ResponseEntity.status(400).build();
            }
        }
        return ResponseEntity.ok(storageResourceDTO);
    }

    // TODO: add exeception handler
}
