package com.vectorlabs.controller;

import static org.hamcrest.Matchers.endsWith;
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
import com.vectorlabs.security.jwt.JwtAuthenticationFilter;
import com.vectorlabs.security.jwt.JwtService;
import com.vectorlabs.service.AppUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(AppUserController.class)
@AutoConfigureMockMvc(addFilters = false) // <- evita briga com Security/PreAuthorize agora
class AppUserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean
    JwtService jwtService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean AppUserService service;
    @MockBean SecurityService securityService;
    @MockBean AppUserMapper mapper;

    private static final String BASE = "/api/users/internal";

    // =========================================================
    // Helpers
    // =========================================================

    private AppUser user(UUID id) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setEmail("sandro@x.com");
        u.setName("Sandro");
        u.setPassword("ENC");
        u.setEnabled(true);
        u.setDeleted(false);
        u.setAuthProvider(AuthProvider.LOCAL);
        u.setRoles(Set.of(UserRole.CLIENT));
        u.setEmailVerified(true);
        u.setLastLoginAt(Instant.parse("2025-01-01T00:00:00Z"));
        u.setCpf("12345678901");
        u.setCnpj(null);
        u.setProviderUserId(null);
        u.setPictureUrl(null);
        u.setAddress(null);
        return u;
    }

    private AnswerAppUserDTO answerDto(UUID id) {
        // ORDEM IMPORTA (record)
        return new AnswerAppUserDTO(
                id,
                "sandro@x.com",
                AuthProvider.LOCAL,
                null, // providerUserId
                "Sandro",
                null, // pictureUrl
                true, // emailVerified
                Instant.parse("2025-01-01T00:00:00Z"),
                "12345678901",
                null, // cnpj
                null, // address (AnswerAddressDTO)
                false, // deleted
                true,  // enabled
                Set.of(UserRole.CLIENT)
        );
    }

    /**
     * Injeta um principal do tipo CustomUserDetails diretamente (sem filtros).
     * Se seu CustomUserDetails tiver outro construtor, ajuste aqui.
     */
    private RequestPostProcessor authPrincipal(AppUser user) {
        CustomUserDetails principal = new CustomUserDetails(user);
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of() // sem authorities porque addFilters=false
        );
        return request -> {
            request.setUserPrincipal(auth);
            return request;
        };
    }
    // =========================================================
    // Tests
    // =========================================================

    @Test
    void register_shouldReturn201_andLocationHeader_andBody() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
        {
          "email": "sandro@x.com",
          "password": "12345678",
          "name": "Sandro",
          "address": null
        }
        """;
        AppUser saved = user(id);
        AnswerAppUserDTO out = answerDto(id);

        when(service.register(any(RegisterAppUserDTO.class))).thenReturn(saved);
        when(mapper.toAnswerDTO(saved)).thenReturn(out);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())

                // ✅ aceita Location como URL absoluta (http://localhost/...)
                .andExpect(header().string("Location", endsWith(BASE + "/" + id)))

                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("sandro@x.com"))
                .andExpect(jsonPath("$.name").value("Sandro"))
                .andExpect(jsonPath("$.authProvider").value("LOCAL"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.deleted").value(false));

        verify(service).register(any(RegisterAppUserDTO.class));
        verify(mapper).toAnswerDTO(saved);
        verifyNoMoreInteractions(service, mapper);
    }

    @Test
    void getDetails_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();
        AppUser entity = user(id);
        AnswerAppUserDTO out = answerDto(id);

        when(service.findById(id)).thenReturn(entity);
        when(mapper.toAnswerDTO(entity)).thenReturn(out);

        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("sandro@x.com"))
                .andExpect(jsonPath("$.name").value("Sandro"));

        verify(service).findById(id);
        verify(mapper).toAnswerDTO(entity);
        verifyNoMoreInteractions(service, mapper);
    }

    @Test
    void getMe_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();

        AppUser logged = user(id);
        AnswerAppUserDTO out = answerDto(id);

        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUser()).thenReturn(logged);
        when(principal.getAuthorities()).thenReturn(java.util.List.of());

        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // <-- isso garante que @AuthenticationPrincipal não fica null
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(mapper.toAnswerDTO(logged)).thenReturn(out);

        mockMvc.perform(get(BASE + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("sandro@x.com"));

        verify(mapper).toAnswerDTO(logged);
        verifyNoInteractions(service, securityService);
        verifyNoMoreInteractions(mapper);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateMe_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();

        AppUser logged = user(id);

        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                "Sandro 2",
                "https://img.com/p.png",
                "12345678901",
                null,
                null
        );

        AppUser updated = user(id);
        updated.setName("Sandro 2");
        updated.setPictureUrl("https://img.com/p.png");

        AnswerAppUserDTO out = answerDto(id);

        // ✅ principal do tipo que o controller espera
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUser()).thenReturn(logged);
        when(principal.getAuthorities()).thenReturn(java.util.List.of());

        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(service.updateMe(eq(dto), eq(logged))).thenReturn(updated);
        when(mapper.toAnswerDTO(updated)).thenReturn(out);

        mockMvc.perform(put(BASE + "/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).updateMe(eq(dto), eq(logged));
        verify(mapper).toAnswerDTO(updated);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(securityService);

        SecurityContextHolder.clearContext();
    }

    @Test
    void update_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                "Admin Updated",
                null,
                "12345678901",
                null,
                null
        );

        AppUser updated = user(id);
        updated.setName("Admin Updated");
        AnswerAppUserDTO out = answerDto(id);

        when(service.update(eq(id), eq(dto))).thenReturn(updated);
        when(mapper.toAnswerDTO(updated)).thenReturn(out);

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).update(eq(id), eq(dto));
        verify(mapper).toAnswerDTO(updated);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(securityService);
    }

    @Test
    void search_shouldReturn200_andPagedBody() throws Exception {
        UUID id = UUID.randomUUID();
        AppUser entity = user(id);
        AnswerAppUserDTO out = answerDto(id);

        Page<AppUser> page = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 20, Sort.by("email").ascending()),
                1
        );

        when(service.search(any(SearchAppUserDTO.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toAnswerDTO(entity)).thenReturn(out);

        mockMvc.perform(get(BASE + "/search")
                        .param("email", "sandro@x.com")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "email,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].email").value("sandro@x.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).search(any(SearchAppUserDTO.class), any(Pageable.class));
        verify(mapper).toAnswerDTO(entity);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(securityService);
    }

    @Test
    void patch_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                "Patch Name",
                null,
                null,
                null,
                null
        );

        AppUser updated = user(id);
        updated.setName("Patch Name");
        AnswerAppUserDTO out = answerDto(id);

        when(service.patch(eq(id), any(UpdateAppUserDTO.class))).thenReturn(updated);
        when(mapper.toAnswerDTO(updated)).thenReturn(out);

        mockMvc.perform(patch(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).patch(eq(id), any(UpdateAppUserDTO.class));
        verify(mapper).toAnswerDTO(updated);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(securityService);
    }

    @Test
    void softDelete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).softDelete(id);

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).softDelete(id);
        verifyNoMoreInteractions(service);
        verifyNoInteractions(mapper, securityService);
    }

    @Test
    void delete_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();

        AppUser deleted = user(id);
        AnswerAppUserDTO out = answerDto(id);

        when(service.deleteById(id)).thenReturn(deleted);
        when(mapper.toAnswerDTO(deleted)).thenReturn(out);

        mockMvc.perform(delete(BASE + "/{id}/hard", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).deleteById(id);
        verify(mapper).toAnswerDTO(deleted);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(securityService);
    }

    @Test
    void deleteMe_shouldReturn200_andBody() throws Exception {
        UUID id = UUID.randomUUID();
        AppUser logged = user(id);

        AppUser result = user(id);
        result.setDeleted(true);

        AnswerAppUserDTO out = answerDto(id);

        when(securityService.getLoggedUser()).thenReturn(logged);
        when(service.softDeleteMe(logged)).thenReturn(result);
        when(mapper.toAnswerDTO(result)).thenReturn(out);

        mockMvc.perform(delete(BASE + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(securityService).getLoggedUser();
        verify(service).softDeleteMe(logged);
        verify(mapper).toAnswerDTO(result);
        verifyNoMoreInteractions(securityService, service, mapper);
    }
}
