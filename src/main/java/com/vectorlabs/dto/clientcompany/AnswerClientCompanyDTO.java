package com.vectorlabs.dto.clientcompany;


import java.time.Instant;
import java.util.UUID;

public record AnswerClientCompanyDTO(
        UUID id,
        UUID userId,
        UUID addressId,
        String corporateName,
        String tradeName,
        String cnpj,
        String phone,
        String email,
        Instant createdAt,
        Instant updatedAt
) {}
