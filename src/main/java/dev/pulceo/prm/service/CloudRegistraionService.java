package dev.pulceo.prm.service;

import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.CloudRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloudRegistraionService {

    private final CloudRegistrationRepository cloudRegistrationRepository;

    @Autowired
    public CloudRegistraionService(CloudRegistrationRepository cloudRegistrationRepository) {
        this.cloudRegistrationRepository = cloudRegistrationRepository;
    }

    CloudRegistration createCloudRegistration(CloudRegistration cloudRegistration) {
        // TODO: check if cloudregistration already exists for device with UUID
        return this.cloudRegistrationRepository.save(cloudRegistration);
    }



}
