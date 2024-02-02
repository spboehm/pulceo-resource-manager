package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.link.AbstractLinkDTO;
import dev.pulceo.prm.dto.link.NodeLinkDTO;
import dev.pulceo.prm.model.link.AbstractLink;
import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/links")
public class LinksController {

    private LinkService linkService;

    @Autowired
    public LinksController(LinkService linkService) {
        this.linkService = linkService;
    }

    // TODO: typing
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
    @GetMapping("/{uuid}")
    public ResponseEntity<AbstractLinkDTO> readLinkByUUID(@PathVariable UUID uuid) {
        Optional<AbstractLink> nodeLink = linkService.readLinkByUUID(uuid);
        if (nodeLink.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(200).body(NodeLinkDTO.fromNodeLink((NodeLink) nodeLink.get()));
    }

}
