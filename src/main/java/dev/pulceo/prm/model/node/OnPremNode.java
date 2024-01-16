package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.BaseEntity;
import dev.pulceo.prm.model.provider.OnPremProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

public class OnPremNode extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private OnPremProvider onPremProvider;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NodeMetaData nodeMetaData;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Node node;
}
