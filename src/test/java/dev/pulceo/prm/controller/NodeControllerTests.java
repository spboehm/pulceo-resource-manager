package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.node.CreateNewAbstractNodeDTO;
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.dto.node.CreateNewOnPremNodeDTO;
import dev.pulceo.prm.dto.node.NodeDTOType;
import dev.pulceo.prm.dto.pna.node.cpu.CPUResourceDTO;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.model.provider.ProviderMetaData;
import dev.pulceo.prm.model.provider.ProviderType;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.repository.AzureProviderRepository;
import dev.pulceo.prm.repository.CloudRegistrationRepository;
import dev.pulceo.prm.service.ProviderService;
import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    static void setupClass() {
        SimulatedPulceoNodeAgent.createAgents(2);
    }

    @AfterEach
    void after() {
        // SimulatedPulceoNodeAgent.resetAgents();
        this.azureProviderRepository.deleteAll();
    }

    @AfterAll
    static void clean() {
        SimulatedPulceoNodeAgent.stopAgents();
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
                .build();
        String createNewOnPremNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewOnPremNodeDTO);

        // when and then
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremNodeDTOAsJson))
                .andExpect(status().isCreated())
                .andReturn();
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

}
