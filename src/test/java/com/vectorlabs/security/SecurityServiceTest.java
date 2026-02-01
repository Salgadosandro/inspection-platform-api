package com.vectorlabs.security;

import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.service.AppUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private AppUserService appUserService;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(appUserService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getLoggedUser_shouldThrowWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        ObjectNotFound ex = assertThrows(ObjectNotFound.class, () -> securityService.getLoggedUser());
        assertTrue(ex.getMessage().contains("No authentication"));
        verifyNoInteractions(appUserService);
    }

    @Test
    void getLoggedUser_shouldReturnUserWhenJwtAuthentication() {
        UUID userId = UUID.randomUUID();
        AppUser expected = new AppUser();
        expected.setId(userId);

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", userId.toString()) // subject UUID
        );

        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(appUserService.findById(userId)).thenReturn(expected);

        AppUser result = securityService.getLoggedUser();

        assertSame(expected, result);
        verify(appUserService).findById(userId);
    }

    @Test
    void getLoggedUser_shouldReturnUserWhenBasicAuthWithCustomUserDetails() {
        UUID userId = UUID.randomUUID();
        AppUser expected = new AppUser();
        expected.setId(userId);

        AppUser principalUser = new AppUser();
        principalUser.setId(userId);

        CustomUserDetails principal = new CustomUserDetails(principalUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "pwd", principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(appUserService.findById(userId)).thenReturn(expected);

        AppUser result = securityService.getLoggedUser();

        assertSame(expected, result);
        verify(appUserService).findById(userId);
    }

    @Test
    void getLoggedUser_shouldThrowWhenUnsupportedPrincipal() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("stringPrincipal", null);

        SecurityContextHolder.getContext().setAuthentication(auth);

        ObjectNotFound ex = assertThrows(ObjectNotFound.class, () -> securityService.getLoggedUser());
        assertTrue(ex.getMessage().contains("Unsupported authentication principal"));
        verifyNoInteractions(appUserService);
    }

    @Test
    void getLoggedUser_shouldThrowWhenJwtSubjectIsNotUuid() {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "a@a.com") // não é UUID
        );

        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalArgumentException.class, () -> securityService.getLoggedUser());
        verifyNoInteractions(appUserService);
    }
}