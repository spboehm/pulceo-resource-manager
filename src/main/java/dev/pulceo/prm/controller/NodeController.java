package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.dto.pna.node.CPU.CPUResourceDTO;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.CPUResource;
import dev.pulceo.prm.model.node.InternalNodeType;
import dev.pulceo.prm.model.node.OnPremNode;
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

    @Autowired
    public NodeController(NodeService nodeService, ModelMapper modelMapper) {
        this.nodeService = nodeService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    public ResponseEntity<AbstractNodeDTO> createNode(@Valid @RequestBody CreateNewAbstractNodeDTO createNewAbstractNodeDTO) throws NodeServiceException {
        if (createNewAbstractNodeDTO.getNodeType() == NodeDTOType.ONPREM) {
            this.logger.info("Received request to create a new OnPremNode: " + createNewAbstractNodeDTO);
            CreateNewOnPremNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.fromAbstractNodeDTO(createNewAbstractNodeDTO);
            OnPremNode onPremNode = this.nodeService.createOnPremNode(createNewOnPremNodeDTO.getProviderName(), createNewOnPremNodeDTO.getHostname(), createNewOnPremNodeDTO.getPnaInitToken());
            return new ResponseEntity<>(NodeDTO.fromOnPremNode(onPremNode), HttpStatus.CREATED);
        } else {
            logger.info("Received request to create a new node of type: " + createNewAbstractNodeDTO.getNodeType());
            throw new NodeServiceException("Node type not yet supported!");
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<AbstractNodeDTO> readNode(@PathVariable UUID uuid) {
        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        InternalNodeType internalNodeType = abstractNode.get().getInternalNodeType();
        if (internalNodeType == InternalNodeType.ONPREM) {
            OnPremNode onPremNode = this.nodeService.readOnPremNode(abstractNode.get().getId());
            return new ResponseEntity<>(NodeDTO.fromOnPremNode(onPremNode), HttpStatus.OK);
        }
        return ResponseEntity.status(400).build();
    }

    @GetMapping("/{uuid}/cpu")
    public ResponseEntity<CPUResourceDTO> readCPUResources(@PathVariable UUID uuid) throws NodeServiceException {
        Optional<AbstractNode> abstractNode = this.nodeService.readAbstractNodeByUUID(uuid);
        if (abstractNode.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        CPUResource cpuResource = this.nodeService.readCPUResourceByUUID(uuid);
        return new ResponseEntity<>(CPUResourceDTO.fromCPUResource(abstractNode.get().getUuid(),abstractNode.get().getNode().getName() , cpuResource), HttpStatus.OK);
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
            }
        }
        return ResponseEntity.status(200).body(abstractNodeDTOList);
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
