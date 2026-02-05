package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.rule.RegisterRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.model.Rule;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RuleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RuleRepository ruleRepository;

    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @BeforeEach
    void setup() {
        ruleRepository.deleteAll();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateRuleSuccessfully() throws Exception {
        RegisterRuleDTO dto = new RegisterRuleDTO(
                "NR12",
                "NR-12",
                "Segurança no trabalho em máquinas e equipamentos",
                "Portaria X",
                LocalDate.of(2024, 1, 1),
                true
        );

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/rules/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("NR12"))
                .andExpect(jsonPath("$.title").value("NR-12"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenOnCreateWhenNotAdmin() throws Exception {
        RegisterRuleDTO dto = new RegisterRuleDTO(
                "NR12", "NR-12", "desc", "Portaria X", LocalDate.of(2024, 1, 1), true
        );

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ------------------- DETAILS -------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetDetailsSuccessfully() throws Exception {
        Rule saved = new Rule();
        saved.setCode("NR13");
        saved.setTitle("NR-13");
        saved.setDescription("Caldeiras e vasos de pressão");
        saved.setActive(true);
        saved.setDeleted(false);
        saved = ruleRepository.save(saved);

        mockMvc.perform(get("/api/rules/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.code").value("NR13"))
                .andExpect(jsonPath("$.title").value("NR-13"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenOnDetailsWhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/rules/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // ------------------- GET ALL (SEARCH) -------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllSuccessfully() throws Exception {
        Rule r1 = new Rule();
        r1.setCode("NR10");
        r1.setTitle("NR-10");
        r1.setDescription("Eletricidade");
        r1.setActive(true);
        r1.setDeleted(false);

        Rule r2 = new Rule();
        r2.setCode("NR12");
        r2.setTitle("NR-12");
        r2.setDescription("Máquinas");
        r2.setActive(true);
        r2.setDeleted(false);

        ruleRepository.saveAll(List.of(r1, r2));

        // sem filtros: só verifica estrutura e que vem algo
        mockMvc.perform(get("/api/rules")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenOnGetAllWhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isForbidden());
    }

    // ------------------- UPDATE -------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRuleSuccessfully() throws Exception {
        Rule saved = new Rule();
        saved.setCode("NR12");
        saved.setTitle("NR-12");
        saved.setDescription("Old");
        saved.setActive(true);
        saved.setDeleted(false);
        saved = ruleRepository.save(saved);

        UpdateRuleDTO dto = new UpdateRuleDTO(
                "NR-12 Atualizada",
                "Nova descrição",
                "Portaria Y",
                LocalDate.of(2025, 1, 1),
                false
        );


        mockMvc.perform(put("/api/rules/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.title").value("NR-12 Atualizada"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenOnUpdateWhenNotAdmin() throws Exception {
        UpdateRuleDTO dto = new UpdateRuleDTO(
                "X",
                "Y",
                "Portaria",
                LocalDate.of(2025, 1, 1),
                true
        );


        mockMvc.perform(put("/api/rules/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ------------------- DELETE (SOFT DELETE) -------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSoftDeleteRuleSuccessfully() throws Exception {
        Rule saved = new Rule();
        saved.setCode("NR20");
        saved.setTitle("NR-20");
        saved.setActive(true);
        saved.setDeleted(false);
        saved = ruleRepository.save(saved);

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/rules/{id}", id))
                .andExpect(status().isNoContent());

        // garante que ainda existe (soft delete) OU que foi marcado como deleted.
        // Ajuste aqui conforme sua implementação.
        Rule after = ruleRepository.findById(id).orElseThrow();

        // Se o softDelete marca deleted=true:
        assertTrue(after.getDeleted(), "Esperava deleted=true após softDelete");
        // se também desativa:
        // assertFalse(after.isActive());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenOnDeleteWhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/rules/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
