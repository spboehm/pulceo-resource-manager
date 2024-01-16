package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.provider.CreateNewAzureProviderDTO;
import dev.pulceo.prm.dtos.ProviderDTOUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProviderMetaDataControllerTests {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCreateAzureProvider() throws Exception {
        // given
        CreateNewAzureProviderDTO createNewAzureProviderDTO = ProviderDTOUtil.createNewAzureProviderDTO();
        String createNewAzureProviderDTOAsJson = objectMapper.writeValueAsString(createNewAzureProviderDTO);

        // when and then
        this.mockMvc.perform(post("/api/v1/providers/azure-providers")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewAzureProviderDTOAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.providerName").value("azure-test"))
                .andExpect(jsonPath("$.providerType").value("AZURE"));
    }
}
