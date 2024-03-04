package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.link.*;
import dev.pulceo.prm.model.link.AbstractLink;
import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/links")
public class LinksController {
    private final Logger logger = LoggerFactory.getLogger(LinksController.class);
    private LinkService linkService;
    @Autowired
    public LinksController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    public ResponseEntity<AbstractLinkDTO> createLink(@RequestBody CreateNewAbstractLinkDTO createNewAbstractLinkDTO) throws Exception {
        if (createNewAbstractLinkDTO.getLinkType() == LinkTypeDTO.NODE_LINK) {
            this.logger.info("Received request to create a new NodeLink: " + createNewAbstractLinkDTO);
            CreateNewNodeLinkDTO createNewNodeLinkDTO = CreateNewNodeLinkDTO.fromAbstractLinkDTO(createNewAbstractLinkDTO);
            NodeLink nodeLink = this.linkService.createNodeLinkById(createNewNodeLinkDTO.getName(), createNewNodeLinkDTO.getSrcNodeId(), createNewNodeLinkDTO.getDestNodeId());
            return ResponseEntity.status(201).body(NodeLinkDTO.fromNodeLink(nodeLink));
        } else {
            logger.info("Received request to create a new link of type: " + createNewAbstractLinkDTO.getLinkType());
            throw new Exception("Link type not yet supported!");
        }
    }

    // TODO: typing and move to service
    @GetMapping("")
    public ResponseEntity<List<AbstractLinkDTO>> readLinkByUUID() {
        List<AbstractLink> links = linkService.readAllLinks();
        List<AbstractLinkDTO> linksDTO = new ArrayList<>();
        for (AbstractLink link : links) {
            if (link instanceof NodeLink) {
                linksDTO.add(NodeLinkDTO.fromNodeLink((NodeLink) link));
            }
        }
        return ResponseEntity.status(200).body(linksDTO);
    }

    // TODO: typing
    @GetMapping("/{id}")
    public ResponseEntity<AbstractLinkDTO> readLinkByUUID(@PathVariable String id) {
        Optional<AbstractLink> nodeLink = this.resolveAbstractLink(id);
        if (nodeLink.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(200).body(NodeLinkDTO.fromNodeLink((NodeLink) nodeLink.get()));
    }

    private Optional<AbstractLink> resolveAbstractLink(String id) {
        Optional<AbstractLink> abstractLink;
        // TODO: add resolve to name here, heck if UUID
        if (checkIfUUID(id)) {
            abstractLink = this.linkService.readLinkByUUID(UUID.fromString(id));
        } else {
            abstractLink = this.linkService.readLinkByName(id);
        }
        return abstractLink;
    }

    private static boolean checkIfUUID(String uuid)  {
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidRegex);
    }

}
