package dev.pulceo.prm.model.link;

import dev.pulceo.prm.internal.G6.model.G6LinkRepresentation;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLink extends BaseEntity implements G6LinkRepresentation {

}
