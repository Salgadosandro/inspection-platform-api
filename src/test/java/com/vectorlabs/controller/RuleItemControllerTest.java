package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.rule.ruleitem.RegisterRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleItemRepository;
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
class RuleItemControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired RuleRepository ruleRepository;
    @Autowired RuleSectionRepository ruleSectionRepository;
    @Autowired RuleModuleRepository ruleModuleRepository;
    @Autowired RuleItemRepository ruleItemRepository;

    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private Rule rule;
    private RuleSection section;
    private RuleModule module;

    @BeforeEach
    void setup() {
        ruleItemRepository.deleteAll();
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

        module = new RuleModule();
        module.setSection(section);
        module.setModuleCode("M1");
        module.setModuleName("Módulo 1");
        module.setModuleSequence(1);
        module.setActive(true);
        module = ruleModuleRepository.save(module);
    }

    // ------------------- CREATE -------------------

    @Test
    void shouldReturnForbiddenOnCreateWhenNotAuthenticated() throws Exception {
        RegisterRuleItemDTO dto = new RegisterRuleItemDTO(
                module.getId(),
                null,            // parentId
                "I1",            // itemCode
                "Descrição 1",   // description
                1                // itemSequence
        );

        mockMvc.perform(post("/api/rule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldCreateRuleItemSuccessfully() throws Exception {
        RegisterRuleItemDTO dto = new RegisterRuleItemDTO(
                module.getId(),
                null,
                "I1",
                "Descrição 1",
                1
        );

        mockMvc.perform(post("/api/rule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/rule-items/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemCode").value("I1"))
                .andExpect(jsonPath("$.description").value("Descrição 1"))
                .andExpect(jsonPath("$.itemSequence").value(1));
    }

    // ------------------- DETAILS -------------------

    @Test
    void shouldReturnForbiddenOnDetailsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rule-items/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetDetailsSuccessfully() throws Exception {
        RuleItem saved = new RuleItem();
        saved.setModule(module);
        saved.setParent(null);
        saved.setItemCode("I2");
        saved.setDescription("Descrição 2");
        saved.setSequence(2);
        saved.setActive(true);
        saved = ruleItemRepository.save(saved);

        mockMvc.perform(get("/api/rule-items/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.itemCode").value("I2"))
                .andExpect(jsonPath("$.description").value("Descrição 2"))
                .andExpect(jsonPath("$.itemSequence").value(2));
    }

    // ------------------- GET ALL (SEARCH) -------------------

    @Test
    void shouldReturnForbiddenOnGetAllWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rule-items"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetAllSuccessfully_withFilters() throws Exception {
        RuleItem a = new RuleItem();
        a.setModule(module);
        a.setParent(null);
        a.setItemCode("A1");
        a.setDescription("Alpha");
        a.setSequence(1);
        a.setActive(true);

        RuleItem b = new RuleItem();
        b.setModule(module);
        b.setParent(null);
        b.setItemCode("B1");
        b.setDescription("Beta");
        b.setSequence(2);
        b.setActive(true);

        ruleItemRepository.saveAll(List.of(a, b));

        mockMvc.perform(get("/api/rule-items")
                        .param("module_id", module.getId().toString())
                        .param("active", "true")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // ------------------- UPDATE -------------------

    @Test
    void shouldReturnForbiddenOnUpdateWhenNotAuthenticated() throws Exception {
        UpdateRuleItemDTO dto = new UpdateRuleItemDTO(
                "I1-ALT",
                "Descrição alterada",
                10,
                false,
                null // parentId
        );

        mockMvc.perform(put("/api/rule-items/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldUpdateRuleItemSuccessfully() throws Exception {
        RuleItem saved = new RuleItem();
        saved.setModule(module);
        saved.setParent(null);
        saved.setItemCode("I1");
        saved.setDescription("Descrição 1");
        saved.setSequence(1);
        saved.setActive(true);
        saved = ruleItemRepository.save(saved);

        UpdateRuleItemDTO dto = new UpdateRuleItemDTO(
                "I1-ALT",
                "Descrição alterada",
                10,
                false,
                null // parentId
        );

        mockMvc.perform(put("/api/rule-items/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.itemCode").value("I1-ALT"))
                .andExpect(jsonPath("$.description").value("Descrição alterada"))
                .andExpect(jsonPath("$.itemSequence").value(10))
                .andExpect(jsonPath("$.active").value(false));
    }

    // ------------------- DELETE -------------------

    @Test
    void shouldReturnForbiddenOnDeleteWhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/rule-items/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldDeleteRuleItemSuccessfully() throws Exception {
        RuleItem saved = new RuleItem();
        saved.setModule(module);
        saved.setParent(null);
        saved.setItemCode("DEL1");
        saved.setDescription("To delete");
        saved.setSequence(1);
        saved.setActive(true);
        saved = ruleItemRepository.save(saved);

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/rule-items/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(ruleItemRepository.findById(id).isPresent(),
                "Esperava que o registro fosse removido do banco");
    }
}
