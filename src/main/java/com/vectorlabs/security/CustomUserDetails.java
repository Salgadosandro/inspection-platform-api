package com.vectorlabs.security;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean deleted;
    private final AppUser user;
    private final Set<UserRole> roles;

    public CustomUserDetails(AppUser user) {
        Objects.requireNonNull(user, "AppUser cannot be null");
        this.user = user;                 // ✅ ESSENCIAL
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.deleted = user.getDeleted();
        this.roles = user.getRoles();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        // Convenção do Spring: ROLE_ADMIN, ROLE_CLIENT, etc.
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        // Para Basic Auth, precisa ter password.
        // Se for OAuth2-only (password null), a autenticação por senha deve falhar naturalmente.
        return password;
    }

    @Override
    public String getUsername() {
        // Para Basic, o "username" será o email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // se você não controla expiração
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // se você não controla lock
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // se você não controla expiração de credenciais
    }

    @Override
    public boolean isEnabled() {
        // IMPORTANTÍSSIMO: bloquear acesso se estiver soft-deletado
        return enabled && !deleted;
    }

    public AppUser getUser() {
        return user;
    }
}

