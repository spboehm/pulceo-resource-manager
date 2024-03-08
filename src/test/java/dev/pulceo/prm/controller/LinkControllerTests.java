package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.link.CreateNewAbstractLinkDTO;
import dev.pulceo.prm.dto.link.CreateNewNodeLinkDTO;
import dev.pulceo.prm.dto.link.LinkTypeDTO;
import dev.pulceo.prm.dto.node.CreateNewAbstractNodeDTO;
import dev.pulceo.prm.dto.node.CreateNewOnPremNodeDTO;
import dev.pulceo.prm.dto.node.NodeDTOType;
import dev.pulceo.prm.repository.AbstractLinkRepository;
import dev.pulceo.prm.repository.AbstractNodeRepository;
import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "webclient.scheme=http"})
@AutoConfigureMockMvc
public class LinkControllerTests {

    @Autowired
    private AbstractNodeRepository abstractNodeRepository;

    @Autowired
    private AbstractLinkRepository abstractLinkRepository;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

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
    public void testCreateNodeLink() throws Exception {
        // first node
        CreateNewAbstractNodeDTO createNewOnPremNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
                .name("test-node")
                .providerName("default")
                .hostname("127.0.0.1")
                .pnaInitToken("pna-init-token")
                .build();
        String createNewOnPremSrcNodeDTOAsJson = this.objectMapper.writeValueAsString(createNewOnPremNodeDTO);
        MvcResult createNewOnPremSrcNodeResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremSrcNodeDTOAsJson))
                .andExpect(status().isCreated())
                .andReturn();
        UUID srcNodeUUID = UUID.fromString(objectMapper.readTree(createNewOnPremSrcNodeResult.getResponse().getContentAsString()).get("uuid").asText());

        // second node
        CreateNewAbstractNodeDTO createNewOnPremDestNodeDTO = CreateNewOnPremNodeDTO.builder()
                .nodeType(NodeDTOType.ONPREM)
                .name("test-node2")
                .providerName("default")
                .hostname("127.0.0.2")
                .pnaInitToken("pna-init-token")
                .build();
        String createNewOnPremDestNodeDTOAsJSON = this.objectMapper.writeValueAsString(createNewOnPremDestNodeDTO);

        MvcResult createNewOnPremDestNodeResult = this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremDestNodeDTOAsJSON))
                .andExpect(status().isCreated())
                .andReturn();
        UUID destNodeUUID = UUID.fromString(objectMapper.readTree(createNewOnPremDestNodeResult.getResponse().getContentAsString()).get("uuid").asText());

        // link
        CreateNewAbstractLinkDTO createNewAbstractLinkDTO = CreateNewNodeLinkDTO.builder()
                .name("test-link")
                .srcNodeId(String.valueOf(srcNodeUUID))
                .destNodeId(String.valueOf(destNodeUUID))
                .build();
        String createNewNodeLinkDTOAsJson = this.objectMapper.writeValueAsString(createNewAbstractLinkDTO);

        // when and then
        this.mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewNodeLinkDTOAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test-link"))
                .andExpect(jsonPath("$.linkUUID", matchesPattern(uuidRegex)))
                .andExpect(jsonPath("$.srcNodeUUID", matchesPattern(uuidRegex)))
                .andExpect(jsonPath("$.destNodeUUID", matchesPattern(uuidRegex)))
                .andExpect(jsonPath("$.linkType", matchesPattern("NODE_LINK")));
    }

}
