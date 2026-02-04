package com.vectorlabs.controller;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.rule.AnswerRuleDTO;
import com.vectorlabs.dto.rule.RegisterRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.mapper.RuleMapper;
import com.vectorlabs.model.Rule;
import com.vectorlabs.security.jwt.JwtAuthenticationFilter;
import com.vectorlabs.security.jwt.JwtService;
import com.vectorlabs.service.RuleService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.util.List;
import org.springframework.data.domain.*;

@WebMvcTest(RuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== Dependencies do controller =====
    @MockBean
    private RuleService ruleService;

    @MockBean
    private RuleMapper ruleMapper;

    // ===== Se sua SecurityConfiguration referencia esses beans, mantenha =====
    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping;

    @Test
    void debug_mappings() {
        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            System.out.println(info + " -> " + method);
        });
    }

    @Test
    void shouldCreateRuleSuccessfully() throws Exception {
        // DTO de entrada
        RegisterRuleDTO dto = new RegisterRuleDTO(
                "NR12-001",
                "Proteção de partes móveis",
                "Descrição de teste",
                "Portaria X",
                java.time.LocalDate.parse("2022-12-20"),
                true
        );

        // Entity que o mapper vai produzir
        Rule toSave = new Rule();
        toSave.setCode(dto.code());

        // Entity "salva" que o service retorna
        UUID id = UUID.randomUUID();
        Rule saved = new Rule();
        saved.setId(id);

        AnswerRuleDTO out = new AnswerRuleDTO(
                id,
                "NR12-001",
                "Proteção de partes móveis",
                "Descrição de teste",
                "Portaria X",
                LocalDate.parse("2022-12-20"),
                true,
                false
        );

        // stubs IMPORTANTES (senão vira null e dá NPE)
        when(ruleMapper.fromRegisterDTO(Mockito.any(RegisterRuleDTO.class))).thenReturn(toSave);
        when(ruleService.save(Mockito.any(Rule.class))).thenReturn(saved);
        when(ruleMapper.toDTO(Mockito.any(Rule.class))).thenReturn(out);

        mockMvc.perform(
                        post("/api/rules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated()); // 201
    }

    @Test
    void shouldReturn403WhenNotAuthenticatedOrNotAdmin() throws Exception {

        RegisterRuleDTO inputDto = new RegisterRuleDTO(
                "NR12-001",
                "Proteção de partes móveis",
                "Descrição de teste",
                "Portaria X",
                LocalDate.of(2022, 12, 20),
                true
        );

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetRuleDetailsSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        // entidade retornada pelo service
        Rule found = new Rule();
        found.setId(id);

        // DTO retornado pelo mapper (record = 8 args)
        AnswerRuleDTO out = new AnswerRuleDTO(
                id,
                "NR12-001",
                "Título",
                "Descrição",
                "Portaria MTP nº 4.219, de 20/12/2022",
                LocalDate.of(2022, 12, 20),
                true,
                false
        );

        when(ruleService.findById(id)).thenReturn(found);
        when(ruleMapper.toDTO(found)).thenReturn(out);

        mockMvc.perform(get("/api/rules/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("NR12-001"))
                .andExpect(jsonPath("$.title").value("Título"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.deleted").value(false));

        verify(ruleService).findById(id);
        verify(ruleMapper).toDTO(found);
        verifyNoMoreInteractions(ruleService, ruleMapper);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRuleSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateRuleDTO dto = new UpdateRuleDTO(
                "Novo título",
                "Nova descrição",
                "Portaria MTP nº 4.219, de 20/12/2022",
                LocalDate.of(2022, 12, 20),
                true
        );

        AnswerRuleDTO out = new AnswerRuleDTO(
                id,
                "NR12-001",
                "Novo título",
                "Nova descrição",
                "Portaria MTP nº 4.219, de 20/12/2022",
                LocalDate.of(2022, 12, 20),
                true,
                false
        );

        when(ruleService.update(eq(id), any(UpdateRuleDTO.class))).thenReturn(out);

        mockMvc.perform(
                        put("/api/rules/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Novo título"))
                .andExpect(jsonPath("$.description").value("Nova descrição"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.deleted").value(false));

        verify(ruleService).update(eq(id), any(UpdateRuleDTO.class));
        verifyNoMoreInteractions(ruleService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteRuleSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(ruleService).softDelete(id);

        mockMvc.perform(delete("/api/rules/{id}", id))
                .andExpect(status().isNoContent());

        verify(ruleService).softDelete(id);
        verifyNoMoreInteractions(ruleService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllRulesSuccessfully_withFilters() throws Exception {
        // params
        String code = "NR12";
        String title = "Segurança";
        String description = "máquina";
        Boolean active = true;
        Boolean deleted = false;
        Integer page = 0;
        Integer pageSize = 10;

        // page mock
        AnswerRuleDTO dto1 = new AnswerRuleDTO(
                UUID.randomUUID(),
                "NR12-001",
                "Segurança em Prensas",
                "Checklist de prensas",
                "Portaria MTP nº 4.219, de 20/12/2022",
                LocalDate.of(2022, 12, 20),
                true,
                false
        );
        AnswerRuleDTO dto2 = new AnswerRuleDTO(
                UUID.randomUUID(),
                "NR12-002",
                "Segurança em Tornos",
                "Checklist de tornos",
                null,
                null,
                true,
                false
        );

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("code").ascending());
        Page<AnswerRuleDTO> resultPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(ruleService.search(
                eq(code),
                eq(title),
                eq(description),
                eq(active),
                eq(deleted),
                eq(page),
                eq(pageSize)
        )).thenReturn(resultPage);

        mockMvc.perform(
                        get("/api/rules")
                                .param("code", code)
                                .param("title", title)
                                .param("description", description)
                                .param("active", String.valueOf(active))
                                .param("deleted", String.valueOf(deleted))
                                .param("page", String.valueOf(page))
                                .param("page_size", String.valueOf(pageSize))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                // Spring Page JSON
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                // itens
                .andExpect(jsonPath("$.content[0].code").value("NR12-001"))
                .andExpect(jsonPath("$.content[0].title").value("Segurança em Prensas"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].deleted").value(false))
                .andExpect(jsonPath("$.content[1].code").value("NR12-002"));

        verify(ruleService).search(
                eq(code),
                eq(title),
                eq(description),
                eq(active),
                eq(deleted),
                eq(page),
                eq(pageSize)
        );
        verifyNoMoreInteractions(ruleService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllRulesSuccessfully_withDefaults() throws Exception {
        // quando não manda page/page_size, o controller manda 0 e 10
        Page<AnswerRuleDTO> empty = Page.empty(PageRequest.of(0, 10, Sort.by("code").ascending()));

        when(ruleService.search(
                isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq(0), eq(10)
        )).thenReturn(empty);

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(ruleService).search(
                isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq(0), eq(10)
        );
        verifyNoMoreInteractions(ruleService);
    }
}
