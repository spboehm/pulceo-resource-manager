package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.node.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NodeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewOnPremNodeDTOAsJson))
                .andExpect(status().isCreated());

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
        this.mockMvc.perform(post("/api/v1/nodes")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewAzureNodeDTOAsJson))
                .andExpect(status().isCreated());

    }
}
