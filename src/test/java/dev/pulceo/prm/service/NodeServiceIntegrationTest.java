package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.util.NodeUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NodeServiceIntegrationTest {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Value("${pna-1.test.init.token}")
    private String pnaInitToken;

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7676));

    @BeforeAll
    static void setupClass() {
        wireMockServer.start();
    }

    @AfterEach
    void after() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void clean() {
        wireMockServer.shutdown();
    }

    @BeforeEach
    public void prepare() {
        this.abstractNodeRepository.deleteAll();
    }

    private UUID pnaUUID = UUID.fromString("0247fea1-3ca3-401b-8fa2-b6f83a469680");
    private String pnaToken = "dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl";
    private UUID prmUUID = UUID.fromString("ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a");
    private String prmEndpoint = "http://localhost:7878";

    @Test
    public void testCreateOnPremNode() throws NodeServiceException {
        // given
        String providerName = "default";
        String hostName = "127.0.0.1";
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-1-cloud-registration-response.json")));
        OnPremNode expectedOnPremNode = NodeUtil.createTestOnPremNode(pnaUUID, hostName, prmUUID, prmEndpoint, pnaToken);

        // when
        OnPremNode onPremNode = this.nodeService.createOnPremNode(providerName, hostName, pnaInitToken);

        // then
        assertEquals(expectedOnPremNode, onPremNode);
    }

}
