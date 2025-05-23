package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Fault;
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.node.NodeDTOType;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.exception.LinkServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.exception.ProviderServiceException;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.OnPremProviderRepository;
import dev.pulceo.prm.util.NodeUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = { "webclient.scheme=http"})
public class NodeServiceIntegrationTest {

    @MockBean
    private AzureDeploymentService azureDeploymentService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;
    @Autowired
    private AbstractLinkRepository abstractLinkRepository;
    @Autowired
    private AzureProviderRepository azureProviderRepository;
    @Autowired
    private OnPremProviderRepository onPremProviderRepository;

    @Value("${pna1.test.uuid}")
    private UUID pna1UUID;

    @Value("${pna1.test.remote.uuid}")
    private UUID pna1RemoteUUID;

    @Value("${pna2.test.uuid}")
    private UUID pna2UUID;

    @Value("${pna1.test.init.token}")
    private String pnaInitToken;

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7676));

    public static WireMockServer wireMockServerForPMS = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7777));
    public static WireMockServer wireMockServerForPSM = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7979));
    @BeforeAll
    static void setupClass() throws InterruptedException {
        Thread.sleep(500);
        wireMockServer.start();
        wireMockServerForPMS.start();
        wireMockServerForPSM.start();
    }

    @AfterEach
    void after() {
//        wireMockServer.resetAll();
    }

    @AfterAll
    static void clean() {
        wireMockServer.shutdown();
        wireMockServerForPMS.shutdown();
        wireMockServerForPSM.shutdown();
    }

    @BeforeEach
    public void prepare() {
        this.abstractLinkRepository.deleteAll();
        this.abstractNodeRepository.deleteAll();
//        this.onPremProviderRepository.deleteAll();/**/
        this.azureProviderRepository.deleteAll();
    }

    @Test
    public void testCreateOnPremNode() throws NodeServiceException, InterruptedException {
        // given
        String providerName = "default";
        String hostName = "127.0.0.1";
        String name = "edge0";
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-1-cloud-registration-response.json")));

        // read local cpu resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/cpu"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-cpu-resource-response.json")));

        // read local memory resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/memory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-memory-resources-response.json")));

        // read local storage resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/storage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-storage-resources-response.json")));

        wireMockServerForPSM.stubFor(post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        OnPremNode expectedOnPremNode = NodeUtil.createTestOnPremNode(name, pna1RemoteUUID, pna1UUID, hostName, "Germany", "Bavaria", "Bamberg");
        expectedOnPremNode.getNode().setLatitude(1.0);
        expectedOnPremNode.getNode().setLongitude(2.0);

        // when
        OnPremNode onPremNode = this.nodeService.createOnPremNode(name, providerName, hostName, pnaInitToken, "edge", "Germany", "Bavaria", "Bamberg", 1.0, 2.0, new ArrayList<>());


        // then
        assertEquals(expectedOnPremNode, onPremNode);
    }

    @Test
    public void testCreatePreliminaryAzureNode() throws NodeServiceException, ProviderServiceException {
        // given
        String providerName = "azure-provider";
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder()
                        .providerName(providerName)
                        .providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().build())
                .build();
        AzureProvider createdAzureProvider = this.providerService.createAzureProvider(azureProvider);

        CreateNewAzureNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName(createdAzureProvider.getProviderMetaData().getProviderName())
                .name("cloud-0")
                .type("cloud")
                .cpu(2)
                .memory(4)
                .region("eastus")
                .build();

        // when
        AzureNode preliminaryAzureNode = this.nodeService.createPreliminaryAzureNode(createNewAzureNodeDTO);

        // then
        // TODO: further evaluations
        assertEquals(preliminaryAzureNode.getNode().getName(), "cloud-0");
    }

    @Test
    public void testReadNodesByNodeType() throws NodeServiceException, InterruptedException, ProviderServiceException {
        // given
        testCreateOnPremNode();
        testCreatePreliminaryAzureNode();

        // when
        List<AbstractNode> edgeNodes = this.nodeService.readNodesByType(NodeType.EDGE);
        List<AbstractNode> cloudNodes = this.nodeService.readNodesByType(NodeType.CLOUD);

        // then
        assert(edgeNodes.size() == cloudNodes.size());
    }

    @Test
    public void testCreateAzureNodeAsync() throws NodeServiceException, ExecutionException, InterruptedException, AzureDeploymentServiceException, ProviderServiceException {
        // given
        String providerName = "azure-provider";
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder()
                        .providerName(providerName)
                        .providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().build())
                .build();
        AzureProvider createdAzureProvider = this.providerService.createAzureProvider(azureProvider);

        // TODO: mock azureDeploymentservice
        when(this.azureDeploymentService.deploy(providerName, "eastus", 2, 4))
                .thenReturn(AzureDeloymentResult.builder()
                        .resourceGroupName("pulceo-node-b6d5536507")
                        .sku("Standard_B2s")
                        .fqdn("127.0.0.1")
                        .build());

        CreateNewAzureNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName(createdAzureProvider.getProviderMetaData().getProviderName())
                .name("cloud-0")
                .type("cloud")
                .cpu(2)
                .memory(4)
                .region("eastus")
                .build();

        wireMockServer.stubFor(get(urlEqualTo("/health"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
                .willSetStateTo("Cause Success"));

        wireMockServer.stubFor(get(urlEqualTo("/health"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Cause Success")
                .willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-1-cloud-registration-response.json")));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/cpu"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-cpu-resource-response.json")));

        // read local memory resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/memory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-memory-resources-response.json")));

        // read local storage resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/storage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-storage-resources-response.json")));

        // when
        AzureNode preliminaryAzureNode = this.nodeService.createPreliminaryAzureNode(createNewAzureNodeDTO);
        CompletableFuture<AzureNode> azureNodeFuture = this.nodeService.createAzureNodeAsync(preliminaryAzureNode.getUuid(), createNewAzureNodeDTO);

        // then
        // TODO: further assertions
        assertEquals(azureNodeFuture.get().getNode().getName(), "cloud-0");
    }

    @Test
    public void testDeleteAzureNode() throws NodeServiceException, ExecutionException, InterruptedException, LinkServiceException, ProviderServiceException, AzureDeploymentServiceException {
        // given
        String providerName = "azure-provider";
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder()
                        .providerName(providerName)
                        .providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().build())
                .build();
        AzureProvider createdAzureProvider = this.providerService.createAzureProvider(azureProvider);

        // TODO: mock azureDeploymentservice
        when(this.azureDeploymentService.deploy(providerName, "eastus", 2, 4))
                .thenReturn(AzureDeloymentResult.builder()
                        .resourceGroupName("pulceo-node-b6d5536507")
                        .sku("Standard_B2s")
                        .fqdn("127.0.0.1")
                        .build());

                CreateNewAzureNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName(createdAzureProvider.getProviderMetaData().getProviderName())
                .name("cloud-0")
                .type("cloud")
                .cpu(2)
                .memory(4)
                .region("eastus")
                .build();

        wireMockServer.stubFor(get(urlEqualTo("/health"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
                .willSetStateTo("Cause Success"));

        wireMockServer.stubFor(get(urlEqualTo("/health"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Cause Success")
                .willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-1-cloud-registration-response.json")));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/cpu"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-cpu-resource-response.json")));

        // read local memory resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/memory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-memory-resources-response.json")));

        // read local storage resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/storage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-storage-resources-response.json")));

        wireMockServerForPMS.stubFor(get(urlMatching("/api/v1/metric-requests\\?linkUUID=(.*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        // when
        AzureNode preliminaryAzureNode = this.nodeService.createPreliminaryAzureNode(createNewAzureNodeDTO);
        CompletableFuture<AzureNode> azureNodeFuture = this.nodeService.createAzureNodeAsync(preliminaryAzureNode.getUuid(), createNewAzureNodeDTO);

        // when
        CompletableFuture<Void> completableFuture = this.nodeService.deleteNodeByUUID(azureNodeFuture.get().getUuid());

        // then
        completableFuture.get();
        assert(this.abstractNodeRepository.findByUuid(azureNodeFuture.get().getUuid()).isEmpty());
    }

    @Test
    public void testGetByRemoteUUID() throws NodeServiceException, InterruptedException {
        // given
        String providerName = "default";
        String hostName = "127.0.0.1";
        String name = "edge0";
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-1-cloud-registration-response.json")));

        // read local cpu resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/cpu"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-cpu-resource-response.json")));

        // read local memory resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/memory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-memory-resources-response.json")));

        // read local storage resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/storage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-storage-resources-response.json")));

        OnPremNode onPremNode = this.nodeService.createOnPremNode(name, providerName, hostName, pnaInitToken, "edge", "Germany", "Bavaria", "Bamberg", 1.0, 2.0, new ArrayList<>());

        // when
        UUID remoteUUID = this.nodeService.getRemoteUUID(onPremNode.getUuid());

        // then
        assertEquals(onPremNode.getNodeMetaData().getRemoteNodeUUID(), remoteUUID);
    }

}
