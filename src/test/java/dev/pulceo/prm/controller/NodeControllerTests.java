package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.node.*;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NodeControllerTests {

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Autowired
    private AbstractLinkRepository abstractLinkRepository;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public void testCreateOnPremNode() throws Exception {
        // given
        CreateNewAbstractNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
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
    public void testCreateAzureNode() throws Exception {
        // given
        CreateNewAbstractNodeDTO createNewAzureNodeDTO = CreateNewAzureNodeDTO.builder()
                .nodeType(NodeDTOType.AZURE)
                .providerName("azure-test")
                .vmSkuType(VMSkuType.Standard_B1S)
                .build();
        String createNewAzureNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewAzureNodeDTO);

        // when and then
        // TODO: implement this
        this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewAzureNodeDTOAsJson))
                .andExpect(status().is4xxClientError());

    }
}
