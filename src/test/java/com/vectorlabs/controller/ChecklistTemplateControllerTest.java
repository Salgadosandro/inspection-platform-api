package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.checklisttemplate.AnswerChecklistTemplateDTO;
import com.vectorlabs.mapper.ChecklistTemplateMapper;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ChecklistTemplate;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import com.vectorlabs.service.ChecklistTemplateService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ChecklistTemplateControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Mantém se sua SecurityConfiguration precisa desse bean no contexto de teste
    @MockitoBean JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @MockitoBean SecurityService securityService;
    @MockitoBean ChecklistTemplateService service;
    @MockitoBean ChecklistTemplateMapper mapper;

    private AppUser loggedUser;
    private UUID userId;

    @BeforeEach
    void setup() {
        loggedUser = AppUser.builder()
                .id(UUID.randomUUID())
                .email("test-" + UUID.randomUUID() + "@vectorlabs.local")
                .enabled(true)
                .deleted(false)
                .build();

        userId = loggedUser.getId();

        // Evita NPE: controller sempre chama securityService.getLoggedUser().getId()
        when(securityService.getLoggedUser()).thenReturn(loggedUser);

        // Limpa interações para cada teste (opcional mas ajuda)
        clearInvocations(service, mapper, securityService);
        // e restaura o stub essencial
        when(securityService.getLoggedUser()).thenReturn(loggedUser);
    }

    // ------------------- CREATE -------------------

    @Test
    void shouldReturnForbiddenOnCreateWhenNotAuthenticated() throws Exception {
        var payload = """
            {
              "ruleId": "%s",
              "title": "Template NR-12",
              "description": "Checklist padrão",
              "active": true
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/checklist-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldReturn422OnCreateWhenPayloadInvalid() throws Exception {
        // payload propositalmente inválido: sem "title" (assumindo @NotBlank) e/ou sem "ruleId"
        var payload = """
            {
              "description": "sem campos obrigatórios"
            }
            """;

        mockMvc.perform(post("/api/checklist-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensagem").value(containsString("Validation")));
    }

    @Test
    @WithMockUser
    void shouldCreateChecklistTemplateSuccessfully() throws Exception {
        UUID ruleId = UUID.randomUUID();
        var payload = """
            {
              "ruleId": "%s",
              "title": "Template NR-12",
              "description": "Checklist padrão",
              "active": true
            }
            """.formatted(ruleId);
        ChecklistTemplate entity = new ChecklistTemplate();
        ChecklistTemplate saved = new ChecklistTemplate();
        UUID savedId = UUID.randomUUID();
        saved.setId(savedId);
        AnswerChecklistTemplateDTO out = mock(AnswerChecklistTemplateDTO.class);
        when(mapper.fromRegisterDTO(any())).thenReturn(entity);
        when(service.save(eq(userId), same(entity))).thenReturn(saved);
        when(mapper.toDTO(same(saved))).thenReturn(out);
        mockMvc.perform(post("/api/checklist-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/checklist-templates/" + savedId)));

        verify(securityService, times(1)).getLoggedUser();
        verify(mapper, times(1)).fromRegisterDTO(any());
        verify(service, times(1)).save(eq(userId), same(entity));
        verify(mapper, times(1)).toDTO(same(saved));
    }

    // ------------------- DETAILS -------------------

    @Test
    void shouldReturnForbiddenOnDetailsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/checklist-templates/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetDetailsSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        ChecklistTemplate entity = new ChecklistTemplate();
        entity.setId(id);

        AnswerChecklistTemplateDTO out = mock(AnswerChecklistTemplateDTO.class);

        when(service.findById(eq(userId), eq(id))).thenReturn(entity);
        when(mapper.toDTO(same(entity))).thenReturn(out);

        mockMvc.perform(get("/api/checklist-templates/{id}", id))
                .andExpect(status().isOk());

        verify(securityService, times(1)).getLoggedUser();
        verify(service, times(1)).findById(userId, id);
        verify(mapper, times(1)).toDTO(entity);
    }

    // ------------------- LIST (SEARCH) -------------------

    @Test
    void shouldReturnForbiddenOnListWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/checklist-templates"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldListChecklistTemplatesSuccessfully_withFilters() throws Exception {
        UUID userFilter = UUID.randomUUID();
        UUID ruleFilter = UUID.randomUUID();

        // DTO real (record exige todos os args na ordem)
        var dto = new AnswerChecklistTemplateDTO(
                UUID.randomUUID(),     // id
                "NR-12",               // title
                "padrão",              // description
                true,                  // active
                false,                 // isDefault
                UUID.randomUUID(),     // userId
                ruleFilter,            // ruleId
                null,                  // createdAt
                null,                  // updatedAt
                null,                  // createdBy
                null                   // updatedBy
        );

        Page<AnswerChecklistTemplateDTO> page =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(dto),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        1
                );

        when(service.search(
                eq(userId),
                eq(userFilter),
                eq(ruleFilter),
                eq("NR-12"),
                eq("padrão"),
                eq(true),
                eq(0),
                eq(10)
        )).thenReturn(page);

        mockMvc.perform(get("/api/checklist-templates")
                        .param("user", userFilter.toString())
                        .param("rule", ruleFilter.toString())
                        .param("title", "NR-12")
                        .param("description", "padrão")
                        .param("active", "true")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk());

        verify(service, times(1)).search(
                userId,
                userFilter,
                ruleFilter,
                "NR-12",
                "padrão",
                true,
                0,
                10
        );
    }
    // ------------------- UPDATE -------------------

    @Test
    void shouldReturnForbiddenOnUpdateWhenNotAuthenticated() throws Exception {
        var payload = """
            {
              "title": "Atualizado",
              "description": "desc",
              "active": false
            }
            """;

        mockMvc.perform(put("/api/checklist-templates/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldUpdateChecklistTemplateSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        var payload = """
            {
              "title": "Atualizado",
              "description": "desc",
              "active": false
            }
            """;

        AnswerChecklistTemplateDTO updated = mock(AnswerChecklistTemplateDTO.class);

        when(service.update(eq(userId), eq(id), any())).thenReturn(updated);

        mockMvc.perform(put("/api/checklist-templates/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(service, times(1)).update(eq(userId), eq(id), any());
    }

    // ------------------- DELETE -------------------

    @Test
    void shouldReturnForbiddenOnDeleteWhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/checklist-templates/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldDeleteChecklistTemplateSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).delete(eq(userId), eq(id));

        mockMvc.perform(delete("/api/checklist-templates/{id}", id))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(userId, id);
    }
}