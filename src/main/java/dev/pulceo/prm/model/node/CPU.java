package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class CPU extends BaseEntity {
    // TODO: add further vendor specific information, model number etc.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPU cpu = (CPU) o;

        if (cores != cpu.cores) return false;
        if (threads != cpu.threads) return false;
        if (Float.compare(bogoMIPS, cpu.bogoMIPS) != 0) return false;
        if (Float.compare(MIPS, cpu.MIPS) != 0) return false;
        if (Float.compare(GFlop, cpu.GFlop) != 0) return false;
        if (Float.compare(minimalFrequency, cpu.minimalFrequency) != 0) return false;
        if (Float.compare(averageFrequency, cpu.averageFrequency) != 0) return false;
        if (Float.compare(maximalFrequency, cpu.maximalFrequency) != 0) return false;
        if (shares != cpu.shares) return false;
        if (Float.compare(slots, cpu.slots) != 0) return false;
        return Objects.equals(modelName, cpu.modelName);
    }

    @Override
    public int hashCode() {
        int result = modelName != null ? modelName.hashCode() : 0;
        result = 31 * result + cores;
        result = 31 * result + threads;
        result = 31 * result + (bogoMIPS != 0.0f ? Float.floatToIntBits(bogoMIPS) : 0);
        result = 31 * result + (MIPS != 0.0f ? Float.floatToIntBits(MIPS) : 0);
        result = 31 * result + (GFlop != 0.0f ? Float.floatToIntBits(GFlop) : 0);
        result = 31 * result + (minimalFrequency != 0.0f ? Float.floatToIntBits(minimalFrequency) : 0);
        result = 31 * result + (averageFrequency != 0.0f ? Float.floatToIntBits(averageFrequency) : 0);
        result = 31 * result + (maximalFrequency != 0.0f ? Float.floatToIntBits(maximalFrequency) : 0);
        result = 31 * result + shares;
        result = 31 * result + (slots != 0.0f ? Float.floatToIntBits(slots) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CPU{" +
                "modelName='" + modelName + '\'' +
                ", cores=" + cores +
                ", threads=" + threads +
                ", bogoMIPS=" + bogoMIPS +
                ", MIPS=" + MIPS +
                ", GFlop=" + GFlop +
                ", minimalFrequency=" + minimalFrequency +
                ", averageFrequency=" + averageFrequency +
                ", maximalFrequency=" + maximalFrequency +
                ", shares=" + shares +
                ", slots=" + slots +
                '}';
    }
}
