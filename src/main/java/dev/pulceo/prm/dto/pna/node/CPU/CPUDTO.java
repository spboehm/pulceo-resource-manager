package dev.pulceo.prm.dto.pna.node.CPU;

import dev.pulceo.prm.model.node.CPU;
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

        public static CPUDTO fromCPU(CPU cpu) {
            return CPUDTO.builder()
                    .modelName(cpu.getModelName())
                    .cores(cpu.getCores())
                    .threads(cpu.getThreads())
                    .bogoMIPS(cpu.getBogoMIPS())
                    .MIPS(cpu.getMIPS())
                    .GFlop(cpu.getGFlop())
                    .minimalFrequency(cpu.getMinimalFrequency())
                    .averageFrequency(cpu.getAverageFrequency())
                    .maximalFrequency(cpu.getMaximalFrequency())
                    .shares(cpu.getShares())
                    .slots(cpu.getSlots())
                    .build();
        }
}
