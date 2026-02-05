package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.auth.LoginDTO;
import com.vectorlabs.dto.auth.RefreshTokenDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean AppUserRepository appUserRepository;
    @MockitoBean
    JwtService jwtService;

    private AppUser userOk(UUID id) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setEmail("user@test.com");
        u.setEnabled(true);
        u.setDeleted(false);
        u.setAuthProvider(AuthProvider.LOCAL);
        u.setRoles(Set.of()); // se precisar
        return u;
    }

    // -------------------------
    // /login
    // -------------------------

    @Test
    void login_ok_returns_tokens_and_updates_lastLogin() throws Exception {
        var dto = new LoginDTO("USER@test.com", "123456");
        var id = UUID.randomUUID();
        var user = userOk(id);

        // auth ok
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        // repo encontra user por email normalizado
        when(appUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        // jwt
        when(jwtService.generateAccessToken(user)).thenReturn("access123");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh123");

        mvc.perform(post("/api/users/internal/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("access123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));

        // garante que salvou lastLoginAt
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertThat(captor.getValue().getLastLoginAt()).isNotNull();
    }

    @Test
    void login_authentication_fails_returns_401() throws Exception {
        var dto = new LoginDTO("user@test.com", "wronggg"); // >= 6

        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("fail"));

        mvc.perform(post("/api/users/internal/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_user_not_found_returns_401() throws Exception {
        var dto = new LoginDTO("user@test.com", "123456");

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);

        when(appUserRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());

        mvc.perform(post("/api/users/internal/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_deleted_or_disabled_returns_401() throws Exception {
        var dto = new LoginDTO("user@test.com", "123456");
        var user = userOk(UUID.randomUUID());
        user.setDeleted(true);

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(appUserRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        mvc.perform(post("/api/users/internal/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------
    // /refresh
    // -------------------------

    @Test
    void refresh_ok_returns_new_tokens() throws Exception {
        var refresh = "refreshToken123";
        var dto = new RefreshTokenDTO(refresh);
        var id = UUID.randomUUID();
        var user = userOk(id);

        doNothing().when(jwtService).validateTokenOrThrow(refresh);
        when(jwtService.isRefreshToken(refresh)).thenReturn(true);
        when(jwtService.extractUserId(refresh)).thenReturn(id);

        when(appUserRepository.findById(id)).thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user)).thenReturn("newAccess");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefresh");

        mvc.perform(post("/api/users/internal/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value("newRefresh"));
    }

    @Test
    void refresh_invalid_signature_or_expired_returns_401() throws Exception {
        var refresh = "bad";
        var dto = new RefreshTokenDTO(refresh);

        doThrow(new BadCredentialsException("Invalid token"))
                .when(jwtService).validateTokenOrThrow(refresh);

        mvc.perform(post("/api/users/internal/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_not_a_refresh_token_returns_401() throws Exception {
        var refresh = "accessDisguised";
        var dto = new RefreshTokenDTO(refresh);

        doNothing().when(jwtService).validateTokenOrThrow(refresh);
        when(jwtService.isRefreshToken(refresh)).thenReturn(false);

        mvc.perform(post("/api/users/internal/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_user_not_found_returns_401() throws Exception {
        var refresh = "refreshToken123";
        var dto = new RefreshTokenDTO(refresh);
        var id = UUID.randomUUID();

        doNothing().when(jwtService).validateTokenOrThrow(refresh);
        when(jwtService.isRefreshToken(refresh)).thenReturn(true);
        when(jwtService.extractUserId(refresh)).thenReturn(id);

        when(appUserRepository.findById(id)).thenReturn(Optional.empty());

        mvc.perform(post("/api/users/internal/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
