package dev.pulceo.prm.model.node;

import dev.pulceo.prm.internal.G6.model.G6NodeRepresentation;
import dev.pulceo.prm.model.BaseEntity;
import dev.pulceo.prm.model.link.NodeLink;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractNode extends BaseEntity implements HasNodeMetaData, G6NodeRepresentation, HasNode, HasToken {

    private InternalNodeType internalNodeType;
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Name must be alphanumeric and can contain hyphens only.")
    private String name;

    @Override
    public String toString() {
        return "AbstractNode{" +
                "internalNodeType=" + internalNodeType +
                ", name='" + name + '\'' +
                '}';
    }
}
