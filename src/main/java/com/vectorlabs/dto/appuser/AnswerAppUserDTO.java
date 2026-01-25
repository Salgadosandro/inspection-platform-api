package com.vectorlabs.dto.appuser;

import com.vectorlabs.dto.address.AnswerAddressDTO;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AnswerAppUserDTO(

        UUID id,
        String email,
        AuthProvider authProvider,
        String providerUserId,
        String name,
        String pictureUrl,
        Boolean emailVerified,
        Instant lastLoginAt,
        String cpf,
        String cnpj,
        AnswerAddressDTO address,
        boolean deleted,
        boolean enabled,
        Set<UserRole> roles
) {}
