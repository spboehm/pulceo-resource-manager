package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.CloudRegistrationRepository;
import dev.pulceo.prm.service.NodeServiceIntegrationTest;
import dev.pulceo.prm.service.ProviderService;
import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "webclient.scheme=http"})
@AutoConfigureMockMvc
public class NodeControllerTests {

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Autowired
    private AbstractLinkRepository abstractLinkRepository;

    @Autowired
    private CloudRegistrationRepository cloudRegistrationRepository;

    @Autowired
    private AzureProviderRepository azureProviderRepository;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${AZURE_SUBSCRIPTION_ID}")
    private String subscriptionId;
    @Value("${AZURE_CLIENT_ID}")
    private String clientId;
    @Value("${AZURE_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${AZURE_TENANT_ID}")
    private String tenantId;

    @BeforeEach
    void before() {
        this.abstractLinkRepository.deleteAll();
        this.abstractNodeRepository.deleteAll();
        this.cloudRegistrationRepository.deleteAll();
    }

    @BeforeAll
    static void setupClass() throws InterruptedException {
        Thread.sleep(500);
        SimulatedPulceoNodeAgent.createAgents(2);
        NodeServiceIntegrationTest.wireMockServerForPSM.start();
    }

    @AfterEach
    void after() {
        // SimulatedPulceoNodeAgent.resetAgents();
        this.azureProviderRepository.deleteAll();
    }

    @AfterAll
    static void clean() {
        SimulatedPulceoNodeAgent.stopAgents();
        NodeServiceIntegrationTest.wireMockServerForPSM.stop();
    }

    @Test
    public void testCreateOnPremNode() throws Exception {
        // given
        CreateNewAbstractNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
                .name("edge0")
                .providerName("default")
                .hostname("127.0.0.1")
                .pnaInitToken("pna-init-token")
                .tags(List.of(NodeTagDTO.builder().key("key-test").value("value-test").build()))
                .build();
        String createNewOnPremNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewOnPremNodeDTO);

        NodeServiceIntegrationTest.wireMockServerForPSM.stubFor(WireMock.post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));


        // when and then
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremNodeDTOAsJson))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void testReadNodesByNodeType() throws Exception {
        // given


    }


    @Test
    public void testReadNodeCpuByUUID() throws Exception {
        // given
        CreateNewAbstractNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
                .providerName("default")
                .hostname("127.0.0.2")
                .pnaInitToken("pna-init-token")
                .build();
        String createNewOnPremNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewOnPremNodeDTO);

        MvcResult nodeResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremNodeDTOAsJson))
                .andExpect(status().isCreated())
                .andReturn();
        UUID srcNodeUUID = UUID.fromString(objectMapper.readTree(nodeResult.getResponse().getContentAsString()).get("uuid").asText());

        NodeServiceIntegrationTest.wireMockServerForPSM.stubFor(WireMock.post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        // when and then
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/nodes/" + srcNodeUUID + "/cpu")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn();
        CPUResourceDTO cpuResourceDTO = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CPUResourceDTO.class);
        assertEquals(12, cpuResourceDTO.getCpuAllocatable().getCores());
        // TODO: further assertions
    }

    @Test
    public void testUpdateNode() throws Exception {
        // given
        CreateNewAbstractNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
                .name("edge0")
                .providerName("default")
                .hostname("127.0.0.1")
                .pnaInitToken("pna-init-token")
                .build();
        String createNewOnPremNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewOnPremNodeDTO);

        NodeServiceIntegrationTest.wireMockServerForPSM.stubFor(WireMock.post(urlEqualTo("/api/v1/applications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(new Body("[]").asJson())));

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremNodeDTOAsJson))
                .andExpect(status().isCreated())
                .andReturn();

        // when and then
        PatchNodeDTO patchNodeDTO = PatchNodeDTO.builder()
                .key("layer")
                .value("2")
                .build();
        String patchNodeDTOAsJson = this.objectMapper.writeValueAsString(patchNodeDTO);

        MvcResult mvcResult2 = this.mockMvc.perform(patch("/api/v1/nodes/" + UUID.fromString(objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("uuid").asText()))
                        .contentType("application/json")
                        .accept("application/json")
                        .content(patchNodeDTOAsJson))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Disabled
    public void testCreateAzureNode() throws Exception {
        // given
        // TODO: create AzureProvider by controller
        AzureProvider azureProvider = AzureProvider.builder()
                .providerMetaData(ProviderMetaData.builder().providerName("azure-provider").providerType(ProviderType.AZURE).build())
                .credentials(AzureCredentials.builder().tenantId(this.tenantId).clientId(this.clientId).clientSecret(this.clientSecret).subscriptionId(this.subscriptionId).build())
                .build();
        AzureProvider actualAzureProvider = this.providerService.createAzureProvider(azureProvider);

        // when
        CreateNewAbstractNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName("azure-provider")
                .name("cloud-0")
                .type("cloud")
                .cpu(2)
                .memory(4)
                .region("eastus")
                .build();

        String createNewAzureNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewAzureNodeDTO);

        // when and then
        this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewAzureNodeDTOAsJson))
                .andExpect(status().is4xxClientError());
    }

    // /* Move this to the right class */
    @Test
    public void readNodeTagsByNode() throws Exception {
        // given
        this.testCreateOnPremNode();

        // when and then
        this.mockMvc.perform(get("/api/v1/nodes/edge0/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagKey").value("key-test"))
                .andExpect(jsonPath("$[0].tagValue").value("value-test"))
                .andReturn();
    }

    @Test
    public void readAllTagsFromCreatedOnPremNode() throws Exception {
        // given
        this.testCreateOnPremNode();

        // when and then
        this.mockMvc.perform(get("/api/v1/tags?type=node"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagKey").value("key-test"))
                .andExpect(jsonPath("$[0].tagValue").value("value-test"))
                .andReturn();
    }

    @Test
    public void testAddNewTagToNode() throws Exception {
        // given
        this.testCreateOnPremNode();

        CreateTagDTO createTagDTO = CreateTagDTO.builder()
                .tagType(TagType.NODE)
                .resourceId("edge0")
                .tagKey("os")
                .tagValue("linux")
                .build();

        // validate created tag
        this.mockMvc.perform(post("/api/v1/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createTagDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagKey").value("os"))
                .andExpect(jsonPath("$.tagValue").value("linux"))
                .andReturn();

        // validate list of tags
        this.mockMvc.perform(get("/api/v1/tags?type=node"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagKey").value("key-test"))
                .andExpect(jsonPath("$[0].tagValue").value("value-test"))
                .andReturn();
    }

    @Test
    public void testAddNewTagToNodeWithInvalidNode() throws Exception {
        // given
        CreateTagDTO createTagDTO = CreateTagDTO.builder()
                .tagType(TagType.NODE)
                .resourceId("edge0")
                .tagKey("os")
                .tagValue("linux")
                .build();

        // when and then
        this.mockMvc.perform(post("/api/v1/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createTagDTO)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testReadTagsByTypeAndKey() throws Exception {
        // given
        this.testCreateOnPremNode();

        CreateTagDTO createTagDTO = CreateTagDTO.builder()
                .tagType(TagType.NODE)
                .resourceId("edge0")
                .tagKey("os")
                .tagValue("linux")
                .build();

        this.mockMvc.perform(post("/api/v1/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createTagDTO)));

        // when and then
        this.mockMvc.perform(get("/api/v1/tags?type=node&key=os"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagKey").value("os"))
                .andExpect(jsonPath("$[0].tagValue").value("linux"))
                .andReturn();
    }

    @Test
    public void testReadTagsByTypeAndKeyAndValue() throws Exception {
        // given
        this.testCreateOnPremNode();

        CreateTagDTO createTagDTO = CreateTagDTO.builder()
                .tagType(TagType.NODE)
                .resourceId("edge0")
                .tagKey("os")
                .tagValue("linux")
                .build();

        this.mockMvc.perform(post("/api/v1/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createTagDTO)));

        // when and then
        this.mockMvc.perform(get("/api/v1/tags?type=node&key=os&value=linux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagKey").value("os"))
                .andExpect(jsonPath("$[0].tagValue").value("linux"))
                .andReturn();
    }

    @Test
    public void testDeleteTag() throws Exception {
        // given
        this.testCreateOnPremNode();

        CreateTagDTO createTagDTO = CreateTagDTO.builder()
                .tagType(TagType.NODE)
                .resourceId("edge0")
                .tagKey("os")
                .tagValue("linux")
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createTagDTO)))
                .andReturn();

        UUID tagUUID = UUID.fromString(objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("tagId").asText());

        // when and then
        this.mockMvc.perform(delete("/api/v1/tags/" + tagUUID))
                .andExpect(status().isNoContent());
    }

}
