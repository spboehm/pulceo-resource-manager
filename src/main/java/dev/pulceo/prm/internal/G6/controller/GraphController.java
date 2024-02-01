package dev.pulceo.prm.internal.G6.controller;

import dev.pulceo.prm.internal.G6.model.G6Data;
import dev.pulceo.prm.internal.G6.model.G6Edge;
import dev.pulceo.prm.internal.G6.model.G6Node;
import dev.pulceo.prm.service.LinkService;
import dev.pulceo.prm.service.NodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/g6")
public class GraphController {

    private final NodeService nodeService;
    private final LinkService linkService;

    @Autowired
    public GraphController(NodeService nodeService, LinkService linkService) {
        this.nodeService = nodeService;
        this.linkService = linkService;
    }

    @GetMapping("/graph/{uuid}")
    @CrossOrigin(origins = "*")
    public G6Data readGraph(@Valid @PathVariable String uuid)  {

        List<G6Node> g6nodes = this.nodeService.readG6NodeData();
        List<G6Edge> g6edges = this.linkService.readG6EdgeData();

        G6Data g6Data = G6Data.builder()
                .g6Nodes(g6nodes)
                .g6Edges(g6edges)
                .build();

        System.out.println(g6Data.toString());

        return g6Data;
    }
    
}
