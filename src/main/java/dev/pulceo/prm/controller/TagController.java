package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.node.CreateTagDTO;
import dev.pulceo.prm.dto.node.NodeTagDTO;
import dev.pulceo.prm.dto.node.TagDTO;
import dev.pulceo.prm.dto.node.TagType;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.NodeTag;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;
    private final NodeService nodeService;

    public TagController(TagService tagService, NodeService nodeService) {
        this.tagService = tagService;
        this.nodeService = nodeService;
    }

    @PostMapping("")
    public ResponseEntity<TagDTO> createTag(@RequestBody CreateTagDTO createTagDTO) {
        // decide if node or link
        if (createTagDTO.getTagType().toString().equals(TagType.NODE.toString())) {
            Optional<AbstractNode> abstractNode = this.resolveAbstractNode(createTagDTO.getResourceId());
            if (abstractNode.isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            // else continue
            try {
                NodeTag createdNodeTag = this.tagService.createNodeTag(NodeTag.fromCreateNodeTagDTO(createTagDTO, abstractNode.get(), abstractNode.get().getNode()));
                return ResponseEntity.status(201).body(TagDTO.fromNodeTag(createdNodeTag));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(400).build();
            }
        } else {
            // TODO: implement for links...
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("")
    public ResponseEntity<List<TagDTO>> readTagsByType(@RequestParam(defaultValue = "node") String tagType) {
        if (tagType.toUpperCase().equals(TagType.NODE.toString())) {

            List<TagDTO> tagDTOs = new ArrayList<>();
            List<NodeTag> nodeTags = this.tagService.readAllNodeTags();
            for (NodeTag nodeTag : nodeTags) {
                tagDTOs.add(TagDTO.fromNodeTag(nodeTag));
            }
            return ResponseEntity.status(200).body(tagDTOs);
        } else {
            // TODO: implement here link tags etc...
            return ResponseEntity.status(400).build();
        }
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

    private static boolean checkIfUUID(String uuid)  {
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidRegex);
    }

}
