package dev.pulceo.prm.controller;

import dev.pulceo.prm.dto.node.CreateTagDTO;
import dev.pulceo.prm.dto.node.TagDTO;
import dev.pulceo.prm.dto.node.TagType;
import dev.pulceo.prm.exception.TagServiceException;
import dev.pulceo.prm.model.node.AbstractNode;
import dev.pulceo.prm.model.node.NodeTag;
import dev.pulceo.prm.service.NodeService;
import dev.pulceo.prm.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<TagDTO> createTag(@RequestBody @Valid CreateTagDTO createTagDTO) throws TagServiceException {
        // decide if node or link
        if (createTagDTO.getTagType().toString().equals(TagType.NODE.toString())) {
            Optional<AbstractNode> abstractNode = this.resolveAbstractNode(createTagDTO.getResourceId());
            if (abstractNode.isEmpty()) {
                throw new TagServiceException("Node with id %s does not exist".formatted(createTagDTO.getResourceId()));
            }

            try {
                NodeTag createdNodeTag = this.tagService.createNodeTag(NodeTag.fromCreateNodeTagDTO(createTagDTO, abstractNode.get(), abstractNode.get().getNode()));
                return ResponseEntity.status(201).body(TagDTO.fromNodeTag(createdNodeTag));
            } catch (TagServiceException e) {
                throw new TagServiceException("Failed to create tag...");
            }
        } else {
            // TODO: implement for links...
            throw new TagServiceException("Only NODE tags are supported at the moment...");
        }
    }

    @GetMapping("")
    public ResponseEntity<List<TagDTO>> readTagsByType(@RequestParam(defaultValue = "node") String tagType, @RequestParam("key") Optional<String> key, @RequestParam("value") Optional<String> value) throws TagServiceException {
        if (tagType.toUpperCase().equals(TagType.NODE.toString())) {

            List<TagDTO> tagDTOs = new ArrayList<>();
            List<NodeTag> nodeTags = this.tagService.readNodeTags(key, value);
            for (NodeTag nodeTag : nodeTags) {
                tagDTOs.add(TagDTO.fromNodeTag(nodeTag));
            }
            return ResponseEntity.status(200).body(tagDTOs);
        } else {
            // TODO: implement here link tags etc...
            throw new TagServiceException("Only NODE tags are supported at the moment...");
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

    @ExceptionHandler(value = TagServiceException.class)
    public ResponseEntity<CustomErrorResponse> handleCloudRegistrationException(TagServiceException tagServiceException) {
        CustomErrorResponse error = new CustomErrorResponse("BAD_REQUEST", tagServiceException.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setErrorMsg(tagServiceException.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
