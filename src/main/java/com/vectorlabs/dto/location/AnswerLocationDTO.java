package com.vectorlabs.dto.location;

import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;

import java.time.Instant;
import java.util.UUID;

public record AnswerLocationDTO(
        UUID id,
        String name,
        String code,
        com.vectorlabs.model.enuns.InspectionLocationType type,
        String description,

        // Address (flattened)
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String country,

        Instant createdAt,
        Instant updatedAt,

        AnswerClientCompanyDTO clientCompany,
        String clientCompanyName
) {}

