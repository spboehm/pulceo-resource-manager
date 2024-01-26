package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.provider.node.AbstractNodeDTO;
import dev.pulceo.prm.dto.provider.node.CreateNewAbstractNodeDTO;
import dev.pulceo.prm.service.NodeService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final NodeService nodeService;
    private final ModelMapper modelMapper;

    @Autowired
    public NodeController(NodeService nodeService, ModelMapper modelMapper) {
        this.nodeService = nodeService;
        this.modelMapper = modelMapper;
    }


    @PostMapping("")
    public ResponseEntity<AbstractNodeDTO> createNode(@Valid @RequestBody CreateNewAbstractNodeDTO createNewAbstractNodeDTO) {
        return ResponseEntity.status(201).build();
    }

}
