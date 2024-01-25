package dev.pulceo.prm.service;


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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class LinkServiceIntegrationTest {

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Autowired
    private AbstractLinkRepository abstractLinkRepository;

    @Autowired
    private LinkService linkService;

    @Autowired
    private NodeService nodeService;

    @Value("${pna1.test.init.token}")
    private String pna1InitToken;

    @Value("${pna2.test.init.token}")
    private String pna2InitToken;

    @BeforeEach
    void before() {
        this.abstractLinkRepository.deleteAll();
        this.abstractNodeRepository.deleteAll();
    }

    @BeforeAll
    static void setupClass() {
        SimulatedPulceoNodeAgent.createAgents(2);
    }

    @AfterEach
    void after() {
        SimulatedPulceoNodeAgent.resetAgents();
    }

    @AfterAll
    static void clean() {
        SimulatedPulceoNodeAgent.stopAgents();
    }

    @Test
    public void testCreateLinkWithExistingNodes() throws NodeServiceException, LinkServiceException {
        // given
        // assume two nodes are already running with simulators
        // create two nodes
        OnPremNode srcNode = NodeUtil.createTestOnPremNode(UUID.randomUUID(), "127.0.0.1");
        OnPremNode destNode = NodeUtil.createTestOnPremNode(UUID.randomUUID(), "127.0.0.2");
        OnPremNode createdSrcOnPremNode = this.nodeService.createOnPremNode(srcNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                srcNode.getNodeMetaData().getHostname(), pna1InitToken);
        OnPremNode createdDestOnPremNode = this.nodeService.createOnPremNode(destNode.getOnPremProvider().getProviderMetaData().getProviderName(),
                destNode.getNodeMetaData().getHostname(), pna2InitToken);
        NodeLink nodeLink = NodeLink.builder().name("testLink").srcNode(createdSrcOnPremNode).destNode(createdDestOnPremNode).build();

        // when
        NodeLink createdNodeLink = this.linkService.createNodeLink(nodeLink);

        // then
        assertEquals(createdNodeLink.getSrcNode(), createdSrcOnPremNode);
        assertEquals(createdNodeLink.getDestNode(), createdDestOnPremNode);
    }

    // TODO: test with cloud nodes

}
