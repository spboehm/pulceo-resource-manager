package dev.pulceo.prm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pulceo.prm.dto.provider.CreateNewProviderDTO;
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
public class ProviderControllerTests {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCreateProvider() throws Exception {
        // given
        CreateNewProviderDTO createNewProviderDTO = ProviderDTOUtil.createNewProviderDTO();
        String createNewProviderDTOAsJson = objectMapper.writeValueAsString(createNewProviderDTO);

        // when and then
        this.mockMvc.perform(post("/api/v1/providers")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(createNewProviderDTOAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.providerName").value("testProvider"));

    }
}
