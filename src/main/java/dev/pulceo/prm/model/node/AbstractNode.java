package dev.pulceo.prm.model.node;

import dev.pulceo.prm.internal.G6.model.G6NodeRepresentation;
import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractNode extends BaseEntity implements HasNodeMetaData, G6NodeRepresentation {

    private InternalNodeType internalNodeType;

}
