package dev.pulceo.prm.controller;

import com.azure.core.annotation.Patch;
import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.dto.node.cpu.PatchCPUDTO;
import dev.pulceo.prm.dto.node.cpu.PatchMemoryDTO;
import dev.pulceo.prm.dto.node.cpu.PatchStorageDTO;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.dto.pna.node.storage.StorageResourceDTO;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.service.AzureDeploymentService;
import dev.pulceo.prm.service.NodeService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nodes")
@CrossOrigin(origins = "*")
public class NodeController {

    private final Logger logger = LoggerFactory.getLogger(NodeController.class);

    private final NodeService nodeService;
    private final ModelMapper modelMapper;

    private final AzureDeploymentService azureDeploymentService;

    @Autowired
    public NodeController(NodeService nodeService, ModelMapper modelMapper, AzureDeploymentService azureDeploymentService) {
        this.nodeService = nodeService;
        this.modelMapper = modelMapper;
        this.azureDeploymentService = azureDeploymentService;
    }

    @PostMapping("")
    public ResponseEntity<AbstractNodeDTO> createNode(@Valid @RequestBody CreateNewAbstractNodeDTO createNewAbstractNodeDTO) throws NodeServiceException {
        if (createNewAbstractNodeDTO.getNodeType() == NodeDTOType.ONPREM) {
            this.logger.info("Received request to create a new OnPremNode: " + createNewAbstractNodeDTO);
            CreateNewOnPremNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.fromAbstractNodeDTO(createNewAbstractNodeDTO);
            OnPremNode onPremNode = this.nodeService.createOnPremNode(createNewOnPremNodeDTO.getName(), createNewOnPremNodeDTO.getProviderName(), createNewOnPremNodeDTO.getHostname(), createNewOnPremNodeDTO.getPnaInitToken(), createNewOnPremNodeDTO.getType(), createNewOnPremNodeDTO.getCountry(), createNewOnPremNodeDTO.getState(), createNewOnPremNodeDTO.getCity());
            return new ResponseEntity<>(NodeDTO.fromOnPremNode(onPremNode), HttpStatus.CREATED);
        } else if (createNewAbstractNodeDTO.getNodeType() == NodeDTOType.AZURE) {
            this.logger.info("Received request to create a new CloudNode: " + createNewAbstractNodeDTO);
            CreateNewAzureNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.fromAbstractNodeDTO(createNewAbstractNodeDTO);
            AzureNode preliminaryAzureNode = this.nodeService.createPreliminaryAzureNode(createNewAzureNodeDTO);
            this.nodeService.createAzureNodeAsync(preliminaryAzureNode.getUuid(), createNewAzureNodeDTO);
            return new ResponseEntity<>(NodeDTO.fromAzureNode(preliminaryAzureNode), HttpStatus.CREATED);
        } else {
            logger.info("Received request to create a new node of type: " + createNewAbstractNodeDTO.getNodeType());
            throw new NodeServiceException("Node type not yet supported!");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbstractNodeDTO> readNode(@PathVariable String id) {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
//        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        InternalNodeType internalNodeType = abstractNode.get().getInternalNodeType();
        if (internalNodeType == InternalNodeType.ONPREM) {
            OnPremNode onPremNode = this.nodeService.readOnPremNode(abstractNode.get().getId());
            return new ResponseEntity<>(NodeDTO.fromOnPremNode(onPremNode), HttpStatus.OK);
        } else if (internalNodeType == InternalNodeType.AZURE) {
            AzureNode azureNode = this.nodeService.readAzureNode(abstractNode.get().getId());
            return new ResponseEntity<>(NodeDTO.fromAzureNode(azureNode), HttpStatus.OK);
        }
        return ResponseEntity.status(400).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NodePropertiesDTO> patchNode(@PathVariable String id, @Valid @RequestBody PatchNodeDTO patchNodeDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);

        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(400).build();
        }

        Node patchedNode = this.nodeService.updateNode(abstractNode.get().getUuid(), patchNodeDTO.getKey(), patchNodeDTO.getValue());

        return new ResponseEntity<>(NodePropertiesDTO.fromNode(patchedNode), HttpStatus.OK);
    }

    private Optional<AbstractNode> resolveAbstractNode(String id) {
        Optional<AbstractNode> abstractNode;
        // TODO: add resolve to name here, heck if UUID
        if (checkIfUUID(id)) {
            abstractNode = this.nodeService.readAbstractNodeByUUID(UUID.fromString(id));
        } else {
            abstractNode = this.nodeService.readAbstractNodeByName(id);
        }
        return abstractNode;
    }

    @GetMapping("/{id}/cpu")
    public ResponseEntity<CPUResourceDTO> readCPUResources(@PathVariable String id) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
//        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        CPUResource cpuResource = this.nodeService.readCPUResourceByUUID(abstractNode.get().getUuid());
        return new ResponseEntity<>(CPUResourceDTO.fromCPUResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , cpuResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/cpu/capacity")
    public ResponseEntity<CPUResourceDTO> updateCPUResourcesCapacity(@PathVariable String id, @Valid @RequestBody PatchCPUDTO patchCPUDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        CPUResource cpuResource = this.nodeService.updateCPUResource(abstractNode.get().getUuid(), patchCPUDTO.getKey(), patchCPUDTO.getValue(), ResourceType.CAPACITY);

        return new ResponseEntity<>(CPUResourceDTO.fromCPUResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , cpuResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/cpu/allocatable")
    public ResponseEntity<CPUResourceDTO> updateCPUResourcesAllocatable(@PathVariable String id, @Valid @RequestBody PatchCPUDTO patchCPUDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        CPUResource cpuResource = this.nodeService.updateCPUResource(abstractNode.get().getUuid(), patchCPUDTO.getKey(), patchCPUDTO.getValue(), ResourceType.ALLOCATABLE);

        return new ResponseEntity<>(CPUResourceDTO.fromCPUResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , cpuResource), HttpStatus.OK);
    }

    @GetMapping("/{id}/memory")
    public ResponseEntity<MemoryResourceDTO> readMemoryResources(@PathVariable String id) throws NodeServiceException {
        // TODO: add resolve to name here, heck if UUID
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
//        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        MemoryResource memoryResource = this.nodeService.readMemoryResourceByUUID(abstractNode.get().getUuid());
        return new ResponseEntity<>(MemoryResourceDTO.fromMemoryResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , memoryResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/memory/capacity")
    public ResponseEntity<MemoryResourceDTO> updateMemoryResourcesCapacity(@PathVariable String id, @Valid @RequestBody PatchMemoryDTO patchMemoryDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        MemoryResource memoryResource = this.nodeService.updateMemoryResource(abstractNode.get().getUuid(), patchMemoryDTO.getKey(), patchMemoryDTO.getValue(), ResourceType.CAPACITY);

        return new ResponseEntity<>(MemoryResourceDTO.fromMemoryResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , memoryResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/memory/allocatable")
    public ResponseEntity<MemoryResourceDTO> updateMemoryResourcesAllocatable(@PathVariable String id, @Valid @RequestBody PatchMemoryDTO patchMemoryDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        MemoryResource memoryResource = this.nodeService.updateMemoryResource(abstractNode.get().getUuid(), patchMemoryDTO.getKey(), patchMemoryDTO.getValue(), ResourceType.ALLOCATABLE);

        return new ResponseEntity<>(MemoryResourceDTO.fromMemoryResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , memoryResource), HttpStatus.OK);
    }

    @GetMapping("/{id}/storage")
    public ResponseEntity<StorageResourceDTO> readStorageResources(@PathVariable String id) throws NodeServiceException {
        // TODO: add resolve to name here, heck if UUID
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
//        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        StorageResource storageResource = this.nodeService.readStorageResourceByUUID(abstractNode.get().getUuid());
        return new ResponseEntity<>(StorageResourceDTO.fromStorageResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , storageResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/storage/capacity")
    public ResponseEntity<StorageResourceDTO> updateStorageResourcesCapacity(@PathVariable String id, @Valid @RequestBody PatchStorageDTO patchStorageDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        StorageResource storageResource = this.nodeService.updateStorageResource(abstractNode.get().getUuid(), patchStorageDTO.getKey(), patchStorageDTO.getValue(), ResourceType.CAPACITY);

        return new ResponseEntity<>(StorageResourceDTO.fromStorageResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , storageResource), HttpStatus.OK);
    }

    @PatchMapping("/{id}/storage/allocatable")
    public ResponseEntity<StorageResourceDTO> updateStorageResourcesAllocatable(@PathVariable String id, @Valid @RequestBody PatchStorageDTO patchStorageDTO) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        StorageResource storageResource = this.nodeService.updateStorageResource(abstractNode.get().getUuid(), patchStorageDTO.getKey(), patchStorageDTO.getValue(), ResourceType.ALLOCATABLE);

        return new ResponseEntity<>(StorageResourceDTO.fromStorageResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , storageResource), HttpStatus.OK);
    }

    @GetMapping("/{id}/pna-token")
    public ResponseEntity<String> readPnaNodeToken(@PathVariable String id) throws NodeServiceException {
        // TODO: add resolve to name here, heck if UUID
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
//        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        return new ResponseEntity<>(abstractNode.get().getToken(), HttpStatus.OK);
    }

    @GetMapping("")
    // effectively, only NodeDTO is returned, but the method signature is kept for future use
    public ResponseEntity<List<AbstractNodeDTO>> readAllNodes() {
        List<AbstractNode> abstractNodeList = this.nodeService.readAllNodes();
        List<AbstractNodeDTO> abstractNodeDTOList = new ArrayList<>();
        for (AbstractNode abstractNode : abstractNodeList) {
            InternalNodeType internalNodeType = abstractNode.getInternalNodeType();
            if (internalNodeType == InternalNodeType.ONPREM) {
                OnPremNode onPremNode = this.nodeService.readOnPremNode(abstractNode.getId());
                abstractNodeDTOList.add(NodeDTO.fromOnPremNode(onPremNode));
            } else if (internalNodeType == InternalNodeType.AZURE) {
                AzureNode azureNode = this.nodeService.readAzureNode(abstractNode.getId());
                abstractNodeDTOList.add(NodeDTO.fromAzureNode(azureNode));
            }
        }
        return ResponseEntity.status(200).body(abstractNodeDTOList);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteNodeById(@PathVariable String id) throws LinkServiceException {
        Optional<AbstractNode> abstractNode = resolveAbstractNode(id);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        this.nodeService.deleteNodeByUUID(abstractNode.get().getUuid());
        return ResponseEntity.status(204).build();
    }

    private static boolean checkIfUUID(String uuid)  {
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidRegex);
    }

    @ExceptionHandler(value = NodeServiceException.class)
    public ResponseEntity<CustomErrorResponse> handleCloudRegistrationException(NodeServiceException nodeServiceException) {
        CustomErrorResponse error = new CustomErrorResponse("BAD_REQUEST", nodeServiceException.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setErrorMsg(nodeServiceException.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
