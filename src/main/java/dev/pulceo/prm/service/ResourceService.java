package dev.pulceo.prm.service;

import dev.pulceo.prm.model.node.CPUResource;
import dev.pulceo.prm.model.node.MemoryResource;
import dev.pulceo.prm.repository.CPUResourcesRepository;
import dev.pulceo.prm.repository.MemoryResourcesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {

    private final CPUResourcesRepository cpuResourcesRepository;
    private final MemoryResourcesRepository memoryResourcesRepository;
    @Autowired
    public ResourceService(CPUResourcesRepository cpuResourcesRepository, MemoryResourcesRepository memoryResourcesRepository) {
        this.cpuResourcesRepository = cpuResourcesRepository;
        this.memoryResourcesRepository = memoryResourcesRepository;
    }

    public Optional<CPUResource> readCPUResourcesById(Long id) {
        return cpuResourcesRepository.findById(id);
    }

    public Optional<MemoryResource> readMemoryResourcesById(Long id) {
        return memoryResourcesRepository.findById(id);
    }

}
