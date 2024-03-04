package dev.pulceo.prm.model.link;

import com.azure.core.annotation.Get;
import dev.pulceo.prm.internal.G6.model.G6LinkRepresentation;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLink extends BaseEntity implements G6LinkRepresentation {

    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Name must be alphanumeric and can contain hyphens only.")
    private String name;

}
