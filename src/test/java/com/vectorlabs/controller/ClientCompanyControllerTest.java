package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import com.vectorlabs.service.ClientCompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientCompanyControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ClientCompanyRepository clientCompanyRepository;

    // Mantém se sua SecurityConfiguration precisa desse bean no contexto de teste
    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;


    @MockitoBean
    ClientCompanyService service;

    // =========================
    // Helpers
    // =========================

    private AnswerClientCompanyDTO sampleOut(UUID id, UUID userId) {
        return new AnswerClientCompanyDTO(
                id,
                userId,
                "ACME Industrial LTDA",
                "ACME",
                "12345678000199",
                "21999999999",
                "contato@acme.com",
                null,           // address (pode ser null)
                true,
                false,
                Instant.now(),
                Instant.now()
        );
    }

    private Map<String, Object> createPayloadMin() {
        // RegisterClientCompanyDTO: obrigatório só corporateName (pelas suas screenshots)
        return Map.of(
                "corporateName", "ACME Industrial LTDA"
        );
    }

    private Map<String, Object> createAdminPayloadMin(UUID userId) {
        // RegisterClientCompanyAdminDTO: obrigatório userId + corporateName
        return Map.of(
                "userId", userId.toString(),
                "corporateName", "ACME Industrial LTDA"
        );
    }

    private Map<String, Object> updatePayloadMin() {
        // UpdateClientCompanyDTO: nada obrigatório
        return Map.of(
                "tradeName", "ACME UPDATED",
                "phone", "21988887777"
        );
    }

    // =========================
    // CREATE (USER)
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void create_ok_returns_201_and_location() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var out = sampleOut(id, userId);

        when(service.create(any(RegisterClientCompanyDTO.class))).thenReturn(out);

        mvc.perform(post("/api/client-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayloadMin())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        endsWith("/api/client-companies/" + id)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.corporateName").value("ACME Industrial LTDA"));

        verify(service).create(any(RegisterClientCompanyDTO.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_missing_corporateName_returns_422() throws Exception {
        mvc.perform(post("/api/client-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(422));

        verifyNoInteractions(service);
    }

    // =========================
    // CREATE (ADMIN)
    // =========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreate_ok_returns_201_and_location() throws Exception {
        UUID id = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        var out = sampleOut(id, targetUserId);

        when(service.adminCreate(any(RegisterClientCompanyAdminDTO.class))).thenReturn(out);

        mvc.perform(post("/api/client-companies/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminPayloadMin(targetUserId))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        endsWith("/api/client-companies/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).adminCreate(any(RegisterClientCompanyAdminDTO.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminCreate_as_user_forbidden_403() throws Exception {
        UUID targetUserId = UUID.randomUUID();

        mvc.perform(post("/api/client-companies/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminPayloadMin(targetUserId))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    // =========================
    // GET DETAILS
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void getDetails_ok_returns_200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var out = sampleOut(id, userId);

        when(service.getDetails(eq(id))).thenReturn(out);

        mvc.perform(get("/api/client-companies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).getDetails(id);
        verifyNoMoreInteractions(service);
    }

    // =========================
    // SEARCH / GET ALL
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void getAll_ok_returns_200_and_page() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var out1 = sampleOut(id1, userId);
        var out2 = sampleOut(id2, userId);

        Page<AnswerClientCompanyDTO> page = new PageImpl<>(
                List.of(out1, out2),
                PageRequest.of(0, 10),
                2
        );

        when(service.search(
                any(), any(), any(), any(), any(), any(), any(),
                eq(0), eq(10)
        )).thenReturn(page);

        mvc.perform(get("/api/client-companies")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(service).search(
                any(), any(), any(), any(), any(), any(), any(),
                eq(0), eq(10)
        );
        verifyNoMoreInteractions(service);
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void update_ok_returns_200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var out = sampleOut(id, userId);

        when(service.update(eq(id), any(UpdateClientCompanyDTO.class))).thenReturn(out);

        mvc.perform(put("/api/client-companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayloadMin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).update(eq(id), any(UpdateClientCompanyDTO.class));
        verifyNoMoreInteractions(service);
    }

    // =========================
    // PATCH: soft-delete / activate / deactivate
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void softDelete_ok_returns_204() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).softDelete(eq(id));

        mvc.perform(patch("/api/client-companies/{id}/soft-delete", id))
                .andExpect(status().isNoContent());

        verify(service).softDelete(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void activate_ok_returns_204() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).activate(eq(id));

        mvc.perform(patch("/api/client-companies/{id}/activate", id))
                .andExpect(status().isNoContent());

        verify(service).activate(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deactivate_ok_returns_204() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).deactivate(eq(id));

        mvc.perform(patch("/api/client-companies/{id}/deactivate", id))
                .andExpect(status().isNoContent());

        verify(service).deactivate(id);
        verifyNoMoreInteractions(service);
    }

    // =========================
    // SECURITY
    // =========================

    @Test
    void any_endpoint_without_auth_returns_401() throws Exception {
        mvc.perform(get("/api/client-companies"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);

    }
}