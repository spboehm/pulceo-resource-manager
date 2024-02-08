package dev.pulceo.prm.service;

import dev.pulceo.prm.model.node.CPUResource;
import dev.pulceo.prm.repository.CPUResourcesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {

    private final CPUResourcesRepository cpuResourcesRepository;
    @Autowired
    public ResourceService(CPUResourcesRepository cpuResourcesRepository) {
        this.cpuResourcesRepository = cpuResourcesRepository;
    }

    public Optional<CPUResource> readCPUResourcesById(Long id) {
        return cpuResourcesRepository.findById(id);
    }


}
