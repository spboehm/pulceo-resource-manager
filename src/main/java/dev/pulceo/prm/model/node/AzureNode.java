package dev.pulceo.prm.model.node;

import dev.pulceo.prm.model.provider.AzureProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

public class AzureNode extends AbstractNode {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AzureProvider azureProvider;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Node node;

}
