package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.rule.rulemodule.RegisterRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.repository.RuleSectionRepository;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RuleModuleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired RuleRepository ruleRepository;
    @Autowired RuleSectionRepository ruleSectionRepository;
    @Autowired RuleModuleRepository ruleModuleRepository;

    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private Rule rule;
    private RuleSection section;

    @BeforeEach
    void setup() {
        ruleModuleRepository.deleteAll();
        ruleSectionRepository.deleteAll();
        ruleRepository.deleteAll();

        rule = new Rule();
        rule.setCode("NR12");
        rule.setTitle("NR-12");
        rule.setDescription("Desc");
        rule.setActive(true);
        rule.setDeleted(false);
        rule = ruleRepository.save(rule);

        section = new RuleSection();
        section.setRule(rule);
        section.setCode("12.1");
        section.setName("Seção 12.1");
        section.setSequence(1);
        section.setActive(true);
        section = ruleSectionRepository.save(section);
    }

    // ------------------- CREATE -------------------

    @Test
    void shouldReturnForbiddenOnCreateWhenNotAuthenticated() throws Exception {
        RegisterRuleModuleDTO dto = new RegisterRuleModuleDTO(
                section.getId(),
                "M1",
                "Módulo 1",
                1,
                true
        );

        mockMvc.perform(post("/api/rule-modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldCreateRuleModuleSuccessfully() throws Exception {
        RegisterRuleModuleDTO dto = new RegisterRuleModuleDTO(
                section.getId(),
                "M1",
                "Módulo 1",
                1,
                true
        );

        mockMvc.perform(post("/api/rule-modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/rule-modules/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.moduleCode").value("M1"))
                .andExpect(jsonPath("$.moduleName").value("Módulo 1"))
                .andExpect(jsonPath("$.moduleSequence").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    // ------------------- DETAILS -------------------

    @Test
    void shouldReturnForbiddenOnDetailsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rule-modules/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetDetailsSuccessfully() throws Exception {
        RuleModule saved = new RuleModule();
        saved.setSection(section);
        saved.setModuleCode("M2");
        saved.setModuleName("Módulo 2");
        saved.setModuleSequence(2);
        saved.setActive(true);
        saved = ruleModuleRepository.save(saved);

        mockMvc.perform(get("/api/rule-modules/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.moduleCode").value("M2"))
                .andExpect(jsonPath("$.moduleName").value("Módulo 2"));
    }

    // ------------------- GET ALL (SEARCH) -------------------

    @Test
    void shouldReturnForbiddenOnGetAllWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rule-modules"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetAllSuccessfully_withFilters() throws Exception {
        RuleModule m1 = new RuleModule();
        m1.setSection(section);
        m1.setModuleCode("M1");
        m1.setModuleName("Módulo 1");
        m1.setModuleSequence(1);
        m1.setActive(true);

        RuleModule m2 = new RuleModule();
        m2.setSection(section);
        m2.setModuleCode("M2");
        m2.setModuleName("Módulo 2");
        m2.setModuleSequence(2);
        m2.setActive(true);

        ruleModuleRepository.saveAll(List.of(m1, m2));

        mockMvc.perform(get("/api/rule-modules")
                        .param("rule", rule.getId().toString())
                        .param("section", section.getId().toString())
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // ------------------- UPDATE -------------------

    @Test
    void shouldReturnForbiddenOnUpdateWhenNotAuthenticated() throws Exception {
        UpdateRuleModuleDTO dto = new UpdateRuleModuleDTO(
                "MX",
                "Novo Nome",
                99,
                false
        );

        mockMvc.perform(put("/api/rule-modules/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldUpdateRuleModuleSuccessfully() throws Exception {
        RuleModule saved = new RuleModule();
        saved.setSection(section);
        saved.setModuleCode("M1");
        saved.setModuleName("Módulo 1");
        saved.setModuleSequence(1);
        saved.setActive(true);
        saved = ruleModuleRepository.save(saved);

        UpdateRuleModuleDTO dto = new UpdateRuleModuleDTO(
                "M1-ALT",
                "Módulo 1 Alterado",
                10,
                false
        );

        mockMvc.perform(put("/api/rule-modules/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.moduleCode").value("M1-ALT"))
                .andExpect(jsonPath("$.moduleName").value("Módulo 1 Alterado"))
                .andExpect(jsonPath("$.moduleSequence").value(10))
                .andExpect(jsonPath("$.active").value(false));
    }

    // ------------------- DELETE -------------------

    @Test
    void shouldReturnForbiddenOnDeleteWhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/rule-modules/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldDeleteRuleModuleSuccessfully() throws Exception {
        RuleModule saved = new RuleModule();
        saved.setSection(section);
        saved.setModuleCode("MDEL");
        saved.setModuleName("Módulo Delete");
        saved.setModuleSequence(1);
        saved.setActive(true);
        saved = ruleModuleRepository.save(saved);

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/rule-modules/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(ruleModuleRepository.findById(id).isPresent(),
                "Esperava que o registro fosse removido do banco");
    }
}
