package com.vectorlabs.dto.appuser;

import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SearchAppUserDTO(
        UUID id,
        String name,
        String email,
        String cpf,
        String cnpj,
        String city,
        String state,
        String country,
        Boolean enabled,
        Boolean deleted,
        AuthProvider authProvider,
        Set<UserRole> roles,
        Instant lastLoginFrom,
        Instant lastLoginTo
) {}
