package dev.pulceo.prm.service;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.link.NodeLink;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.util.NodeUtil;
import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import java.util.ArrayList;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = { "webclient.scheme=http"})
public class LinkServiceIntegrationTest {

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Autowired
    private AbstractLinkRepository abstractLinkRepository;

    @Autowired
    private LinkService linkService;

    @Autowired
    private NodeService nodeService;

    @Value("${pna1.test.remote.uuid}")
    private UUID pna1RemoteUUID;

    @Value("${pna2.test.remote.uuid}")
    private UUID pna2RemoteUUID;

    @Value("${pna1.test.init.token}")
    private String pna1InitToken;

    @Value("${pna2.test.init.token}")
    private String pna2InitToken;

    private UUID pna3RemoteUUID = UUID.randomUUID();
    private String pna3InitToken = UUID.randomUUID().toString();

    public static WireMockServer wireMockServerForPSM = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7979));


    @BeforeEach
    void before() {
        this.abstractLinkRepository.deleteAll();
        this.abstractNodeRepository.deleteAll();
    }

    @BeforeAll
    static void setupClass() throws InterruptedException {
        Thread.sleep(500);
        SimulatedPulceoNodeAgent.createAgents(3);
        wireMockServerForPSM.start();
    }

    @AfterEach
    void after() {
        // SimulatedPulceoNodeAgent.resetAgents();
    }

    @AfterAll
    static void clean() {
        SimulatedPulceoNodeAgent.stopAgents();
        wireMockServerForPSM.stop();
    }

    @Test
    public void testCreateLinkWithExistingNodes() throws NodeServiceException, LinkServiceException, InterruptedException {
        // given
        // assume two nodes are already running with simulators
        // create two nodes

        wireMockServerForPSM.stubFor(WireMock.post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        OnPremNode srcNode = NodeUtil.createTestOnPremNode("edge0", pna1RemoteUUID, UUID.randomUUID(), "127.0.0.1", "Germany", "Bavaria", "Munich");
        OnPremNode destNode = NodeUtil.createTestOnPremNode("edge1", pna2RemoteUUID, UUID.randomUUID(), "127.0.0.2", "Germany", "Bavaria", "Munich");
        OnPremNode createdSrcOnPremNode = this.nodeService.createOnPremNode("edge0", srcNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                srcNode.getNodeMetaData().getHostname(), pna1InitToken, "edge", "Germany", "Bavaria", "Munich", 1.0, 2.0, new ArrayList<>());
        OnPremNode createdDestOnPremNode = this.nodeService.createOnPremNode("edge1", destNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                destNode.getNodeMetaData().getHostname(), pna2InitToken, "edge", "Germany", "Bavaria", "Munich", 1.0, 2.0, new ArrayList<>());
        NodeLink nodeLink = NodeLink.builder().name("testLink").srcNode(createdSrcOnPremNode).destNode(createdDestOnPremNode).build();

        // when
        NodeLink createdNodeLink = this.linkService.createNodeLink(nodeLink);

        // then
        assertEquals(createdNodeLink.getSrcNode(), createdSrcOnPremNode);
        assertEquals(createdNodeLink.getDestNode(), createdDestOnPremNode);
    }

    @Test
    public void testCreateLinkWithThreeNodes() throws NodeServiceException, LinkServiceException, InterruptedException {
        // given
        // assume two nodes are already running with simulators
        // create two nodes
        wireMockServerForPSM.stubFor(WireMock.post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        OnPremNode srcNode = NodeUtil.createTestOnPremNode("edge0", pna1RemoteUUID, UUID.randomUUID(), "127.0.0.1", "Germany", "Bavaria", "Munich");
        OnPremNode destNode = NodeUtil.createTestOnPremNode("edge1", pna2RemoteUUID, UUID.randomUUID(), "127.0.0.2", "Germany", "Bavaria", "Munich");
        OnPremNode destNode2 = NodeUtil.createTestOnPremNode("edge2", pna3RemoteUUID, UUID.randomUUID(), "127.0.0.3", "Germany", "Bavaria", "Munich");
        OnPremNode createdSrcOnPremNode = this.nodeService.createOnPremNode("edge0", srcNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                srcNode.getNodeMetaData().getHostname(), pna1InitToken, "edge", "Germany", "Bavaria", "Munich", 1.0,2.0, new ArrayList<>());
        OnPremNode createdDestOnPremNode = this.nodeService.createOnPremNode("edge1", destNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                destNode.getNodeMetaData().getHostname(), pna2InitToken, "edge", "Germany", "Bavaria", "Munich", 1.0,2.0, new ArrayList<>());
        OnPremNode createdthirdOnPremNode = this.nodeService.createOnPremNode("edge2", destNode2.getOnPremProvider().getProviderMetaData().getProviderName(),
                destNode2.getNodeMetaData().getHostname(), pna3InitToken, "edge", "Germany", "Bavaria", "Munich", 1.0,2.0, new ArrayList<>());

        NodeLink nodeLink = NodeLink.builder().name("testLink").srcNode(createdSrcOnPremNode).destNode(createdDestOnPremNode).build();
        NodeLink nodeLink2 = NodeLink.builder().name("testLink2").srcNode(createdSrcOnPremNode).destNode(createdthirdOnPremNode).build();

        // when
        NodeLink createdNodeLink = this.linkService.createNodeLink(nodeLink);
        NodeLink createdNodeLink2 = this.linkService.createNodeLink(nodeLink2);

        // then
        assertEquals(createdNodeLink.getSrcNode(), createdSrcOnPremNode);
        assertEquals(createdNodeLink.getDestNode(), createdDestOnPremNode);
    }

    // TODO: test with cloud nodes

}
