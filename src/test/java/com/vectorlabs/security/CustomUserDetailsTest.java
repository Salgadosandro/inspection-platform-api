package com.vectorlabs.security;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void constructor_shouldThrow_whenUserIsNull() {
        assertThrows(NullPointerException.class, () -> new CustomUserDetails(null));
    }

    @Test
    void shouldExposeBasicFields_fromUser() {
        UUID id = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("sandro@vectorlabs.com");
        user.setPassword("hash");
        user.setEnabled(true);
        user.setDeleted(false);
        user.setRoles(Set.of(UserRole.ADMIN, UserRole.CLIENT));

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(id, details.getId());
        assertEquals("sandro@vectorlabs.com", details.getEmail());
        assertEquals("hash", details.getPassword());
        assertSame(user, details.getUser());
        assertEquals(Set.of(UserRole.ADMIN, UserRole.CLIENT), details.getRoles());

        // UserDetails
        assertEquals("sandro@vectorlabs.com", details.getUsername());
    }

    @Test
    void isEnabled_shouldBeFalse_whenDeletedIsTrue_evenIfEnabledIsTrue() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setEnabled(true);
        user.setDeleted(true);

        CustomUserDetails details = new CustomUserDetails(user);

        assertFalse(details.isEnabled());
    }

    @Test
    void isEnabled_shouldBeFalse_whenEnabledIsFalse() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setEnabled(false);
        user.setDeleted(false);

        CustomUserDetails details = new CustomUserDetails(user);

        assertFalse(details.isEnabled());
    }

    @Test
    void authorities_shouldBeEmpty_whenRolesNullOrEmpty() {
        AppUser user1 = new AppUser();
        user1.setEmail("a@a.com");
        user1.setEnabled(true);
        user1.setDeleted(false);
        user1.setRoles(null);

        AppUser user2 = new AppUser();
        user2.setEmail("b@b.com");
        user2.setEnabled(true);
        user2.setDeleted(false);
        user2.setRoles(Set.of());

        assertTrue(new CustomUserDetails(user1).getAuthorities().isEmpty());
        assertTrue(new CustomUserDetails(user2).getAuthorities().isEmpty());
    }

    @Test
    void authorities_shouldPrefixROLE_() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setEnabled(true);
        user.setDeleted(false);
        user.setRoles(Set.of(UserRole.ADMIN, UserRole.CLIENT));

        CustomUserDetails details = new CustomUserDetails(user);

        Set<String> auths = details.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        assertTrue(auths.contains("ROLE_ADMIN"));
        assertTrue(auths.contains("ROLE_CLIENT"));
        assertEquals(2, auths.size());
    }

    @Test
    void authorities_shouldIgnoreNullRolesInsideSet() {
        AppUser user = new AppUser();
        user.setEmail("a@a.com");
        user.setEnabled(true);
        user.setDeleted(false);

        var roles = new java.util.HashSet<UserRole>();
        roles.add(UserRole.ADMIN);
        roles.add(null); // agora é permitido
        user.setRoles(roles);

        CustomUserDetails details = new CustomUserDetails(user);

        Set<String> auths = details.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN"), auths);
    }
}