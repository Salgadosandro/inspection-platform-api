package com.vectorlabs.dto.checklisttemplate;
// RegisterChecklistTemplateDTO.java

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterChecklistTemplateDTO(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        UUID userId,

        @NotNull
        UUID ruleId,

        Boolean isDefault
) {}