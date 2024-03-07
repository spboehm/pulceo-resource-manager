package dev.pulceo.prm.service;

import dev.pulceo.prm.model.node.CPUResource;
import dev.pulceo.prm.model.node.MemoryResource;
import dev.pulceo.prm.model.node.Storage;
import dev.pulceo.prm.model.node.StorageResource;
import dev.pulceo.prm.repository.CPUResourcesRepository;
import dev.pulceo.prm.repository.MemoryResourcesRepository;
import dev.pulceo.prm.repository.StorageResourcesRepositoy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {

    private final CPUResourcesRepository cpuResourcesRepository;
    private final MemoryResourcesRepository memoryResourcesRepository;
    private final StorageResourcesRepositoy storageResourcesRepositoy;

    @Autowired
    public ResourceService(CPUResourcesRepository cpuResourcesRepository, MemoryResourcesRepository memoryResourcesRepository, StorageResourcesRepositoy storageResourcesRepositoy) {
        this.cpuResourcesRepository = cpuResourcesRepository;
        this.memoryResourcesRepository = memoryResourcesRepository;
        this.storageResourcesRepositoy = storageResourcesRepositoy;
    }

    public Optional<CPUResource> readCPUResourcesById(Long id) {
        return cpuResourcesRepository.findById(id);
    }

    public Optional<MemoryResource> readMemoryResourcesById(Long id) {
        return memoryResourcesRepository.findById(id);
    }

    public Optional<StorageResource> readStorageResourcesById(Long id) {
        return storageResourcesRepositoy.findById(id);
    }

}
