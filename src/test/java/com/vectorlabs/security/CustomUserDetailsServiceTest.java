package com.vectorlabs.security;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(appUserRepository);
    }

    @Test
    void loadUserByUsername_shouldNormalizeEmailAndReturnCustomUserDetails() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("a@a.com");
        user.setPassword("hashed");
        user.setEnabled(true);
        user.setDeleted(false);
        user.setRoles(Set.of(UserRole.ADMIN));

        when(appUserRepository.findByEmail("a@a.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("  A@A.COM  ");

        assertNotNull(details);
        assertEquals("a@a.com", details.getUsername());
        assertTrue(details.isEnabled());
        assertEquals(Set.of("ROLE_ADMIN"),
                details.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet())
        );

        // garante que normalizou no repository
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(appUserRepository).findByEmail(captor.capture());
        assertEquals("a@a.com", captor.getValue());
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserNotFound() {
        when(appUserRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("x@x.com")
        );

        assertTrue(ex.getMessage().contains("User not found for email: x@x.com"));
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserIsDeleted() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setPassword("hashed");
        user.setEnabled(true);
        user.setDeleted(true);

        when(appUserRepository.findByEmail("a@a.com")).thenReturn(Optional.of(user));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("a@a.com")
        );

        assertTrue(ex.getMessage().contains("User is deleted"));
    }

    @Test
    void loadUserByUsername_shouldThrowWhenPasswordIsNull() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setPassword(null);
        user.setEnabled(true);
        user.setDeleted(false);

        when(appUserRepository.findByEmail("a@a.com")).thenReturn(Optional.of(user));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("a@a.com")
        );

        assertTrue(ex.getMessage().contains("no local password"));
    }

    @Test
    void loadUserByUsername_shouldThrowWhenPasswordIsBlank() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setPassword("   ");
        user.setEnabled(true);
        user.setDeleted(false);

        when(appUserRepository.findByEmail("a@a.com")).thenReturn(Optional.of(user));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("a@a.com")
        );

        assertTrue(ex.getMessage().contains("no local password"));
    }
}