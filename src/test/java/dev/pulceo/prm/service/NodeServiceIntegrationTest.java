package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.pulceo.prm.model.node.OnPremNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

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
        WireMockServer wireMockServer = new WireMockServer(7676);
        wireMockServer.start();
        configureFor("localhost", 7676);
        stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"pnaUUID\":\"0247fea1-3ca3-401b-8fa2-b6f83a469680\",\"prmUUID\":\"ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a\",\"prmEndpoint\":\"http://localhost:7878\",\"pnaToken\":\"dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl\"}")));
        // when
        OnPremNode onPremNode = this.nodeService.createOnPremNode(providerName, hostName, this.pnaInitToken);

        // then
        wireMockServer.stop();

    }

}
