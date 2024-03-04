package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.dto.pna.node.memory.MemoryResourceDTO;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.service.AzureDeploymentService;
import dev.pulceo.prm.service.NodeService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

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
            OnPremNode onPremNode = this.nodeService.createOnPremNode(createNewOnPremNodeDTO.getName(), createNewOnPremNodeDTO.getProviderName(), createNewOnPremNodeDTO.getHostname(), createNewOnPremNodeDTO.getPnaInitToken());
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
