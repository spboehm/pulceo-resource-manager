package dev.pulceo.prm.service;

import dev.pulceo.prm.model.node.OnPremNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NodeServiceIntegrationTest {

    @Autowired
    private NodeService nodeService;

    @Value("${pna.test.init.token}")
    private String pnaInitToken;

    @Test
    public void testCreateOnPremNode() {
        // given
        String providerName = "default";
        String hostName = "localhost";

        // when
        OnPremNode onPremNode = this.nodeService.createOnPremNode(providerName, hostName, this.pnaInitToken);

        // then

    }

}
