package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Fault;
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.node.NodeDTOType;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.event.PulceoEvent;
import dev.pulceo.prm.model.node.*;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.AzureNodeRepository;
import dev.pulceo.prm.repository.NodeMetaDataRepository;
import dev.pulceo.prm.repository.NodeRepository;
import dev.pulceo.prm.util.NodeUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodeServiceUnitTests {

    @Mock
    private NodeMetaDataRepository nodeMetaDataRepository;
    @Mock
    private AbstractNodeRepository abstractNodeRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private ProviderService providerService;
    @Mock
    private CloudRegistraionService cloudRegistraionService;
    @Mock
    private AzureNodeRepository azureNodeRepository;
    @Mock
    private AzureDeploymentService azureDeploymentService;
    @Mock
    private EventHandler eventHandler;
    @InjectMocks
    private NodeService nodeService;

    private UUID pnaUUID = UUID.fromString("0247fea1-3ca3-401b-8fa2-b6f83a469680");
    private String pnaInitToken = "b0hRUGwxT0hNYnhGbGoyQ2tlQnBGblAxOmdHUHM3MGtRRWNsZVFMSmdZclFhVUExb0VpNktGZ296";
    private String pnaToken = "dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl";
    private UUID prmUUID = UUID.fromString("ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a");
    private String prmEndpoint = "http://localhost:7878";
    private String psmEndpoint = "http://localhost:7979";

    private String webClientScheme = "http";

    private UUID pna1RemoteUUID = UUID.fromString("8f08e447-7ccd-4657-a873-a1d43a733b1a");

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.2").port(7676));
    public static WireMockServer wireMockServerForPSM = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.1").port(7979));


    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(nodeService, "prmUUID", prmUUID);
        ReflectionTestUtils.setField(nodeService, "prmEndpoint", prmEndpoint);
        ReflectionTestUtils.setField(nodeService, "pnaInitToken", pnaInitToken);
        ReflectionTestUtils.setField(nodeService, "webClientScheme", webClientScheme);
        ReflectionTestUtils.setField(nodeService, "psmEndpoint", psmEndpoint);
    }

    @BeforeAll
    static void setupClass() throws InterruptedException {
        Thread.sleep(500);
        wireMockServer.start();
        wireMockServerForPSM.start();
    }

    @AfterEach
    void after() {
//        wireMockServer.resetAll();
//        wireMockServerForPSM.resetAll();
    }

    @AfterAll
    static void clean() {
        wireMockServer.shutdown();
        wireMockServerForPSM.shutdown();
    }

    @Test
    public void testCreateOnPremNode() throws NodeServiceException, InterruptedException {
        // given
        String providerName = "default";
        String hostName = "127.0.0.2";
        String name = "edge0";
        when(this.providerService.readOnPremProviderByProviderName(providerName))
                .thenReturn(Optional.of(
                        OnPremProvider.builder()
                                .providerMetaData(ProviderMetaData.builder()
                                        .providerName("default")
                                        .providerType(ProviderType.ON_PREM).build()).build()));
        Mockito.doNothing().when(this.eventHandler).handleEvent(ArgumentMatchers.any(PulceoEvent.class));
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

        // create dummy applications
        wireMockServerForPSM.stubFor(post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        OnPremNode expectedOnPremNode = NodeUtil.createTestOnPremNode(name, pna1RemoteUUID, pnaUUID, hostName, "Germany", "Bavaria", "Munich");
        expectedOnPremNode.getNode().setLatitude(1.0);
        expectedOnPremNode.getNode().setLongitude(2.0);

        // when
        this.nodeService.createOnPremNode(name, providerName, hostName, pnaInitToken, "edge", "Germany", "Bavaria", "Munich", 1.0, 2.0, new ArrayList<>());

        // then
        // TODO: more verifications
        verify(this.abstractNodeRepository, new Times(1)).findByName(name);
        verify(this.abstractNodeRepository, new Times(1)).save(expectedOnPremNode);
    }

    @Test
    // TODO: fix
    public void testCreateAzureNode() throws NodeServiceException, AzureDeploymentServiceException, ExecutionException, InterruptedException {
        // given
        String azureProvider = "azure-provider";
        when(this.providerService.readAzureProviderByProviderMetaDataName(azureProvider))
                .thenReturn(Optional.of(
                        AzureProvider.builder()
                                .providerMetaData(ProviderMetaData.builder()
                                        .providerName(azureProvider)
                                        .providerType(ProviderType.AZURE).build())
                                .build()));

        when(this.azureDeploymentService.deploy(azureProvider, "eastus", 2, 4))
                .thenReturn(AzureDeloymentResult.builder()
                        .resourceGroupName("pulceo-node-b6d5536507")
                        .sku("Standard_B2s")
                        .fqdn("127.0.0.2")
                        .build());

        CreateNewAzureNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName("azure-provider")
                .name("cloud-0")
                .type("cloud")
                .cpu(2)
                .memory(4)
                .region("eastus")
                .build();

        when(azureNodeRepository.save(Mockito.any())).thenReturn(AzureNode.builder().build());
        when(azureNodeRepository.readAzureNodeByUuid(Mockito.any())).thenReturn(Optional.of(AzureNode.builder()
                    .nodeMetaData(NodeMetaData.builder().build())
                    .node(Node.builder().build())
                .build()));
        Mockito.doNothing().when(this.eventHandler).handleEvent(ArgumentMatchers.any(PulceoEvent.class));

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

        // create dummy applications
        wireMockServerForPSM.stubFor(post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        // when
        AzureNode preliminaryAzureNode = this.nodeService.createPreliminaryAzureNode(createNewAzureNodeDTO);
        CompletableFuture<AzureNode> azureNodeFuture = this.nodeService.createAzureNodeAsync(preliminaryAzureNode.getUuid(), createNewAzureNodeDTO);
        azureNodeFuture.get();

        // then
        // TODO: further validations
        verify(this.azureNodeRepository, new Times(1)).readAzureNodeByUuid(Mockito.any());
        verify(this.azureNodeRepository, new Times(2)).save(ArgumentMatchers.any(AzureNode.class));
    }

}