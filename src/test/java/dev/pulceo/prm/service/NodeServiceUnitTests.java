package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.node.NodeDTOType;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.AzureDeloymentResult;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.NodeMetaDataRepository;
import dev.pulceo.prm.repository.NodeRepository;
import dev.pulceo.prm.util.NodeUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

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
    private AzureDeploymentService azureDeploymentService;
    @InjectMocks
    private NodeService nodeService;

    private UUID pnaUUID = UUID.fromString("0247fea1-3ca3-401b-8fa2-b6f83a469680");
    private String pnaInitToken = "b0hRUGwxT0hNYnhGbGoyQ2tlQnBGblAxOmdHUHM3MGtRRWNsZVFMSmdZclFhVUExb0VpNktGZ296";
    private String pnaToken = "dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl";
    private UUID prmUUID = UUID.fromString("ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a");
    private String prmEndpoint = "http://localhost:7878";

    private String webClientScheme = "http";

    private UUID pna1RemoteUUID = UUID.fromString("8f08e447-7ccd-4657-a873-a1d43a733b1a");

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().bindAddress("127.0.0.2").port(7676));

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(nodeService, "prmUUID", prmUUID);
        ReflectionTestUtils.setField(nodeService, "prmEndpoint", prmEndpoint);
        ReflectionTestUtils.setField(nodeService, "pnaInitToken", pnaInitToken);
        ReflectionTestUtils.setField(nodeService, "webClientScheme", webClientScheme);
    }

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

    @Test
    public void testCreateOnPremNode() throws NodeServiceException {
        // given
        String providerName = "default";
        String hostName = "127.0.0.2";
        when(this.providerService.readOnPremProviderByProviderName(providerName))
                .thenReturn(Optional.of(
                        OnPremProvider.builder()
                                .providerMetaData(ProviderMetaData.builder()
                                        .providerName("default")
                                        .providerType(ProviderType.ON_PREM).build()).build()));
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

        OnPremNode expectedOnPremNode = NodeUtil.createTestOnPremNode(pna1RemoteUUID, pnaUUID, hostName);

        // when
        this.nodeService.createOnPremNode(providerName, hostName, pnaInitToken);

        // then
        // TODO: more verifications
        verify(this.abstractNodeRepository, new Times(1)).save(expectedOnPremNode);
    }

    @Test
    @Disabled
    public void testCreateAzureNode() throws NodeServiceException, AzureDeploymentServiceException {
        // given
        String azureProvider = "azure-provider";
        when(this.providerService.readAzureProviderByProviderMetaDataName(azureProvider))
                .thenReturn(Optional.of(
                        AzureProvider.builder()
                                .providerMetaData(ProviderMetaData.builder()
                                        .providerName(azureProvider)
                                        .providerType(ProviderType.AZURE).build())
                                .build()));

        when(this.azureDeploymentService.deploy(azureProvider, "eastus", "Standard_B2s"))
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
                .sku("Standard_B2s")
                .nodeLocationCountry("eastus")
                .nodeLocationCity("Virginia")
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

        // when
//        this.nodeService.createAzureNodeAsync(createNewAzureNodeDTO);

        // then
        // TODO: further validations
//        verify(this.abstractNodeRepository, new Times(1)).save(ArgumentMatchers.any(AzureNode.class));
    }

}
