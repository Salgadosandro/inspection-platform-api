package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.rule.ruledection.RegisterRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.repository.RuleSectionRepository;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import com.vectorlabs.security.jwt.JwtService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RuleSectionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Autowired JwtService jwtService;

    @Autowired AppUserRepository appUserRepository;
    @Autowired RuleRepository ruleRepository;
    @Autowired RuleSectionRepository ruleSectionRepository;

    private String adminToken;
    private UUID ruleId;

    @BeforeEach
    void setup() {
        // 1) cria um RULE real (porque o endpoint recebe ruleId)
        Rule rule = new Rule();
        rule.setCode("R-001");
        rule.setTitle("Regra teste");
        rule = ruleRepository.save(rule);
        ruleId = rule.getId();

        // 2) cria usuário ADMIN real no H2
        AppUser admin = new AppUser();
        admin.setEmail("admin@test.com");
        admin.setPassword("{noop}123"); // ou use seu encoder (ideal)
        admin.setRoles(Set.of(UserRole.ADMIN));     // ajuste conforme seu modelo (enum/collection/etc)
        admin = appUserRepository.save(admin);

        // 3) gera JWT real
        // ✅ ajuste o método conforme seu JwtService (nomes comuns: generateAccessToken / createAccessToken etc)
        adminToken = jwtService.generateAccessToken(admin);
    }

    @Test
    void shouldCreateRuleSectionSuccessfully_withRealJwt() throws Exception {
        RegisterRuleSectionDTO dto = new RegisterRuleSectionDTO(
                ruleId,
                "RS-001",
                "Seção Geral",
                1,
                true
        );

        mockMvc.perform(
                        post("/api/rule-sections")
                                .with(csrf()) // se sua Security não desabilita CSRF
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/rule-sections/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ruleId").value(ruleId.toString()))
                .andExpect(jsonPath("$.code").value("RS-001"))
                .andExpect(jsonPath("$.name").value("Seção Geral"))
                .andExpect(jsonPath("$.sequence").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenNotAdmin() throws Exception {
        UUID anyId = UUID.randomUUID();

        mockMvc.perform(get("/api/rule-sections/{id}", anyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        UUID anyId = UUID.randomUUID();

        mockMvc.perform(get("/api/rule-sections/{id}", anyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRuleSectionSuccessfully() throws Exception {
        // arrange: Rule
        Rule rule = new Rule();
        rule.setCode("NR12");
        rule.setTitle("NR-12");
        rule.setActive(true);
        rule.setDeleted(false);
        rule = ruleRepository.save(rule);

        // arrange: RuleSection existente
        RuleSection section = new RuleSection();
        section.setRule(rule);
        section.setCode("RS-001");
        section.setName("Seção Antiga");
        section.setSequence(1);
        section.setActive(true);
        section = ruleSectionRepository.save(section);

        UUID id = section.getId();

        // DTO de update
        UpdateRuleSectionDTO dto = new UpdateRuleSectionDTO(
                "RS-001",
                "Seção Atualizada",
                2,
                false
        );

        // act + assert
        mockMvc.perform(put("/api/rule-sections/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Seção Atualizada"))
                .andExpect(jsonPath("$.sequence").value(2))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUpdatingWithoutAdminRole() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateRuleSectionDTO dto = new UpdateRuleSectionDTO(
                "RS-001",
                "Qualquer",
                1,
                true
        );

        mockMvc.perform(put("/api/rule-sections/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonExistingRuleSection() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateRuleSectionDTO dto = new UpdateRuleSectionDTO(
                "RS-999",
                "Inexistente",
                1,
                true
        );

        mockMvc.perform(put("/api/rule-sections/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteRuleSectionSuccessfully() throws Exception {
        // arrange: Rule
        Rule rule = new Rule();
        rule.setCode("NR12");
        rule.setTitle("NR-12");
        rule.setActive(true);
        rule.setDeleted(false);
        rule = ruleRepository.save(rule);

        // arrange: RuleSection
        RuleSection section = new RuleSection();
        section.setRule(rule);
        section.setCode("RS-DEL");
        section.setName("Seção Delete");
        section.setSequence(1);
        section.setActive(true);
        section = ruleSectionRepository.save(section);

        UUID id = section.getId();

        // act + assert
        mockMvc.perform(delete("/api/rule-sections/{id}", id))
                .andExpect(status().isNoContent());

        // garante que foi removido
        assertFalse(ruleSectionRepository.existsById(id));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenDeletingWithoutAdminRole() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/rule-sections/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonExistingRuleSection() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/rule-sections/{id}", id))
                .andExpect(status().isNotFound());
    }


}
