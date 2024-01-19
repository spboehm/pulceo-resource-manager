package dev.pulceo.prm.repository;

import dev.pulceo.prm.model.registration.CloudRegistration;
import org.springframework.data.repository.CrudRepository;

public interface CloudRegistrationRepository extends CrudRepository<CloudRegistration, Long> {
}
