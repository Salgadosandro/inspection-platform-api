package com.vectorlabs.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.appuser.AnswerAppUserDTO;
import com.vectorlabs.dto.appuser.RegisterAppUserDTO;
import com.vectorlabs.dto.appuser.SearchAppUserDTO;
import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.mapper.AppUserMapper;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.security.CustomUserDetails;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.security.jwt.JwtAuthenticationConverterConfig;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import com.vectorlabs.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // filtros ON => PreAuthorize sendo aplicado
@ActiveProfiles("test")
class AppUserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AppUserService service;
    @MockitoBean SecurityService securityService;
    @MockitoBean AppUserMapper mapper;
    @MockitoBean JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
    @MockitoBean JwtAuthenticationConverterConfig jwtAuthenticationConverterConfig;

    private AppUser user(UUID id, String email, Set<UserRole> roles) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setEmail(email);
        u.setEnabled(true);
        u.setDeleted(false);
        u.setAuthProvider(AuthProvider.LOCAL);
        u.setRoles(roles);
        u.setLastLoginAt(Instant.parse("2026-02-05T14:00:00Z"));
        u.setName("User Test");
        return u;
    }

    /**
     * Ajuste aqui se seu CustomUserDetails tiver outro construtor/factory.
     */
    private CustomUserDetails principalOf(AppUser u) {
        return new CustomUserDetails(u);
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser() {
        return SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN");
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor normalUser() {
        return SecurityMockMvcRequestPostProcessors.user("user").roles("USER");
    }

    // -------------------------
    // POST /api/users/internal  (register)
    // -------------------------

    @Test
    void register_ok_returns_201_and_location() throws Exception {
        var id = UUID.randomUUID();
        var saved = user(id, "new@test.com", Set.of(UserRole.USER));

        // DTOs: use seus construtores/records reais
        RegisterAppUserDTO in = mock(RegisterAppUserDTO.class); // evita depender do shape do DTO
        AnswerAppUserDTO out = mock(AnswerAppUserDTO.class);

        when(service.register(any())).thenReturn(saved);
        when(mapper.toAnswerDTO(saved)).thenReturn(out);

        mvc.perform(post("/api/users/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "email": "new@test.com",
              "password": "12345678"
            }
        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/users/internal/" + id)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));


        verify(service).register(any(RegisterAppUserDTO.class));
        verify(mapper).toAnswerDTO(saved);
    }

    @Test
    void getDetails_admin_ok() throws Exception {
        var id = UUID.randomUUID();
        var entity = user(id, "user@test.com", Set.of(UserRole.USER));
        AnswerAppUserDTO out = mock(AnswerAppUserDTO.class);

        when(service.findById(id)).thenReturn(entity);
        when(mapper.toAnswerDTO(entity)).thenReturn(out);

        mvc.perform(get("/api/users/internal/{id}", id).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).findById(id);
        verify(mapper).toAnswerDTO(entity);
    }

    @Test
    void getDetails_non_admin_forbidden() throws Exception {
        var id = UUID.randomUUID();

        mvc.perform(get("/api/users/internal/{id}", id).with(normalUser()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
        verifyNoInteractions(mapper);
    }

    // -------------------------
    // GET /api/users/internal/me  (authenticated)
    // -------------------------

    @Test
    void getMe_ok_returns_logged_user() throws Exception {
        var id = UUID.randomUUID();
        var logged = user(id, "me@test.com", Set.of(UserRole.USER));
        var principal = principalOf(logged);
        AnswerAppUserDTO out = mock(AnswerAppUserDTO.class);

        when(mapper.toAnswerDTO(logged)).thenReturn(out);

        mvc.perform(get("/api/users/internal/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(mapper).toAnswerDTO(logged);
        verifyNoInteractions(service);
    }

    // -------------------------
    // PUT /api/users/internal/me  (authenticated)
    // -------------------------

    @Test
    void updateMe_ok() throws Exception {
        var id = UUID.randomUUID();
        var logged = user(id, "me@test.com", Set.of(UserRole.USER));
        var principal = principalOf(logged);

        UpdateAppUserDTO in = mock(UpdateAppUserDTO.class);
        var result = user(id, "me@test.com", Set.of(UserRole.USER));
        AnswerAppUserDTO out = mock(AnswerAppUserDTO.class);

        when(service.updateMe(any(UpdateAppUserDTO.class), eq(logged))).thenReturn(result);
        when(mapper.toAnswerDTO(result)).thenReturn(out);

        mvc.perform(put("/api/users/internal/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).updateMe(any(UpdateAppUserDTO.class), eq(logged));
        verify(mapper).toAnswerDTO(result);
    }

    // -------------------------
    // PUT /api/users/internal/{id}  (ADMIN)
    // -------------------------

    @Test
    void update_admin_ok() throws Exception {
        var id = UUID.randomUUID();
        UpdateAppUserDTO in = mock(UpdateAppUserDTO.class);
        var result = user(id, "updated@test.com", Set.of(UserRole.USER));
        AnswerAppUserDTO out = mock(AnswerAppUserDTO.class);

        when(service.update(eq(id), any(UpdateAppUserDTO.class))).thenReturn(result);
        when(mapper.toAnswerDTO(result)).thenReturn(out);

        mvc.perform(put("/api/users/internal/{id}", id)
                        .with(adminUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).update(eq(id), any(UpdateAppUserDTO.class));
        verify(mapper).toAnswerDTO(result);
    }

    // -------------------------
    // GET /api/users/internal/search  (ADMIN)
    // -------------------------

    @Test
    void search_admin_ok_returns_page() throws Exception {
        var id = UUID.randomUUID();
        var e1 = user(id, "a@test.com", Set.of(UserRole.USER));
        var page = new PageImpl<>(List.of(e1), PageRequest.of(0, 20), 1);

        when(service.search(any(SearchAppUserDTO.class), any())).thenReturn(page);
        when(mapper.toAnswerDTO(e1)).thenReturn(mock(AnswerAppUserDTO.class));

        mvc.perform(get("/api/users/internal/search")
                        .with(adminUser())
                        .param("email", "a@test.com")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Page padrão do Spring tem "content"
                .andExpect(jsonPath("$.content").isArray());

        verify(service).search(any(SearchAppUserDTO.class), any());
        verify(mapper).toAnswerDTO(e1);
    }

    // -------------------------
    // PATCH /api/users/internal/{id} (no PreAuthorize no código)
    // -------------------------

    @Test
    void patch_ok() throws Exception {
        var id = UUID.randomUUID();
        var updated = user(id, "patched@test.com", Set.of(UserRole.USER));
        when(service.patch(eq(id), any(UpdateAppUserDTO.class))).thenReturn(updated);
        when(mapper.toAnswerDTO(updated)).thenReturn(mock(AnswerAppUserDTO.class));

        mvc.perform(patch("/api/users/internal/{id}", id)
                        .with(adminUser()) // por segurança, caso sua HttpSecurity exija auth globalmente
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).patch(eq(id), any(UpdateAppUserDTO.class));
        verify(mapper).toAnswerDTO(updated);
    }

    // -------------------------
    // DELETE /api/users/internal/{id}  (soft delete - sem PreAuthorize no código)
    // -------------------------

    @Test
    void softDelete_ok_returns_204() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(service).softDelete(id);

        mvc.perform(delete("/api/users/internal/{id}", id)
                        .with(adminUser()))
                .andExpect(status().isNoContent());

        verify(service).softDelete(id);
    }

    // -------------------------
    // DELETE /api/users/internal/{id}/hard  (ADMIN)
    // -------------------------

    @Test
    void hardDelete_admin_ok() throws Exception {
        var id = UUID.randomUUID();
        var deleted = user(id, "deleted@test.com", Set.of(UserRole.USER));
        when(service.deleteById(id)).thenReturn(deleted);
        when(mapper.toAnswerDTO(deleted)).thenReturn(mock(AnswerAppUserDTO.class));

        mvc.perform(delete("/api/users/internal/{id}/hard", id)
                        .with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).deleteById(id);
        verify(mapper).toAnswerDTO(deleted);
    }

    // -------------------------
    // DELETE /api/users/internal/me  (authenticated)
    // -------------------------

    @Test
    void deleteMe_ok() throws Exception {
        var id = UUID.randomUUID();
        var logged = user(id, "me@test.com", Set.of(UserRole.USER));
        var result = user(id, "me@test.com", Set.of(UserRole.USER));

        when(securityService.getLoggedUser()).thenReturn(logged);
        when(service.softDeleteMe(logged)).thenReturn(result);
        when(mapper.toAnswerDTO(result)).thenReturn(mock(AnswerAppUserDTO.class));

        mvc.perform(delete("/api/users/internal/me")
                        .with(normalUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(securityService).getLoggedUser();
        verify(service).softDeleteMe(logged);
        verify(mapper).toAnswerDTO(result);
    }
}
