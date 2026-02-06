package com.vectorlabs.dto.clientcompany;


import com.vectorlabs.dto.address.AnswerAddressDTO;

import java.time.Instant;
import java.util.UUID;

public record AnswerClientCompanyDTO(
        UUID id,
        UUID userId,
        String corporateName,
        String tradeName,
        String cnpj,
        String phone,
        String email,
        AnswerAddressDTO address,
        Boolean active,
        Boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {}
