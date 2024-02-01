package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.AbstractNode;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nodes")
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
            CreateNewOnPremNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.fromAbstractNodeDTO(createNewAbstractNodeDTO);
            OnPremNode onPremNode = this.nodeService.createOnPremNode(createNewOnPremNodeDTO.getProviderName(), createNewOnPremNodeDTO.getHostname(), createNewOnPremNodeDTO.getPnaInitToken());
            return new ResponseEntity<>(NodeDTO.fromOnPremNode(onPremNode), HttpStatus.CREATED);
        } else {
            logger.info("Received request to create a new node of type: " + createNewAbstractNodeDTO.getNodeType());
        }
        return ResponseEntity.status(201).build();
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

    @ExceptionHandler(value = NodeServiceException.class)
    public ResponseEntity<CustomErrorResponse> handleCloudRegistrationException(NodeServiceException nodeServiceException) {
        CustomErrorResponse error = new CustomErrorResponse("BAD_REQUEST", nodeServiceException.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
