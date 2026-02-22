package com.vectorlabs.dto.checklisttemplate;
import jakarta.validation.constraints.Size;

public record UpdateChecklistTemplateDTO(

        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        Boolean active,

        Boolean isDefault
) {}