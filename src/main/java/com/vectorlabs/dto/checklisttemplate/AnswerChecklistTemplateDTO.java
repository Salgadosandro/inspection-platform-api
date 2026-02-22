package com.vectorlabs.dto.checklisttemplate;

// AnswerChecklistTemplateDTO.java
import java.time.Instant;
import java.util.UUID;

public record AnswerChecklistTemplateDTO(
        UUID id,
        String title,
        String description,
        Boolean active,
        Boolean isDefault,
        UUID userId,
        UUID ruleId,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy
) {}
