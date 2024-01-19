package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.pulceo.prm.exception.NodeServiceException;
import dev.pulceo.prm.model.node.Node;
import dev.pulceo.prm.model.node.NodeMetaData;
import dev.pulceo.prm.model.node.OnPremNode;
import dev.pulceo.prm.model.provider.OnPremProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.model.registration.CloudRegistration;
import dev.pulceo.prm.repository.NodeMetaDataRepository;
import dev.pulceo.prm.repository.NodeRepository;
import dev.pulceo.prm.repository.OnPremNodeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodeServiceUnitTests {

    @Mock
    private NodeMetaDataRepository nodeMetaDataRepository;
    @Mock
    private OnPremNodeRepository onPremNoderepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private ProviderService providerService;
    @Mock
    private CloudRegistraionService cloudRegistraionService;
    @InjectMocks
    private NodeService nodeService;

    private UUID pnaUUID = UUID.fromString("0247fea1-3ca3-401b-8fa2-b6f83a469680");
    private UUID prmUUID = UUID.fromString("ecda0beb-dba9-4836-a0f8-da6d0fd0cd0a");
    private String prmEndpoint = "http://localhost:7878";

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().port(7676));

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(nodeService, "prmUUID", prmUUID);
        ReflectionTestUtils.setField(nodeService, "prmEndpoint", prmEndpoint);
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
    @Disabled
    public void testCreateOnPremNode() throws NodeServiceException {
        // given
        String providerName = "default";
        String hostName = "localhost";
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
                        .withBodyFile("registration/cloud-registration-response.json")));
        // when
        OnPremNode onPremNode = this.nodeService.createOnPremNode(providerName, hostName, "pnaInitToken");

        // then
        OnPremNode extpectedOnPremNode;
        ProviderMetaData providerMetaData;

        OnPremProvider onPremProvider = OnPremProvider.builder().providerMetaData(
                ProviderMetaData.builder()
                .providerName("default")
                .providerType(ProviderType.ON_PREM)
                .build()).build();
        System.out.println(pnaUUID);
        NodeMetaData nodeMetaData = NodeMetaData.builder()
                .pnaUUID(pnaUUID)
                .hostname("localhost")
                .build();

        Node node = Node.builder()
                .name("localhost")
                .build();

        CloudRegistration cloudRegistration = CloudRegistration.builder().pnaUUID(pnaUUID).prmUUID(prmUUID).prmEndpoint(prmEndpoint).pnaToken("dGppWG5XamMyV2ZXYTBadzlWZ0dvWnVsOjVINHhtWUpNNG1wTXB2YzJaQjlTS2ZnNHRZcWl2OTRl").build();

        extpectedOnPremNode = OnPremNode.builder()
                .onPremProvider(onPremProvider)
                .nodeMetaData(nodeMetaData)
                .node(node)
                .cloudRegistration(cloudRegistration)
                .build();

        verify(this.onPremNoderepository, new Times(1)).save(extpectedOnPremNode);
    }

}
