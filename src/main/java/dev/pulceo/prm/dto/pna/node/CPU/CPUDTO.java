package dev.pulceo.prm.dto.pna.node.CPU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CPUDTO {

        private String modelName;
        private int cores;
        private int threads;
        private float bogoMIPS;
        private float MIPS;
        private float GFlop;
        private float minimalFrequency;
        private float averageFrequency;
        private float maximalFrequency;
        private int shares;
        private float slots;

}
