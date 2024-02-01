package dev.pulceo.prm.internal.G6.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class G6Edge {
    String id;
    String source;
    String target;
    String label;
}
