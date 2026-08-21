package com.lb.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lb.customerservice.dto.CustomerRequest;
import com.lb.customerservice.dto.ScoreResponse;
import com.lb.customerservice.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ScoreService scoreService;

    private String validRequestJson(String cpf) throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .name("Joao da Silva").cpf(cpf).email("joao@email.com").build();
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/customers")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoUserTentaCriar() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("52998224725")))
                .andExpect(status().isForbidden());
    }

    @Test
    void fluxoCompletoCrud() throws Exception {
        String cpf = "35847098065";

        String createResponse = mockMvc.perform(post("/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(cpf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/customers/" + id).with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/customers/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/customers/" + id).with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ComCpfInvalido() throws Exception {
        String invalidJson = validRequestJson("99999999999");
        mockMvc.perform(post("/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornar409ComCpfDuplicado() throws Exception {
        String cpf = "22233445694";
        mockMvc.perform(post("/customers").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(validRequestJson(cpf)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(validRequestJson(cpf)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarScoreViaServicoExterno() throws Exception {
        String cpf = "33344556703";
        String created = mockMvc.perform(post("/customers").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(validRequestJson(cpf)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(created).get("id").asLong();

        when(scoreService.getScoreByCustomerId(id))
                .thenReturn(new ScoreResponse(cpf, 820, "LOW_RISK"));

        mockMvc.perform(get("/customers/" + id + "/score").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(820));
    }
}