package dev.pulceo.prm.internal.G6.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class G6Data {

    @JsonProperty("nodes")
    private List<G6Node> g6Nodes = new ArrayList<>();
    @JsonProperty("edges")
    private List<G6Edge> g6Edges = new ArrayList<>();

}
