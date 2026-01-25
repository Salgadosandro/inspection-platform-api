package com.vectorlabs.dto.rule;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateRuleDTO(

        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 200)
        String updateOrdinance,

        LocalDate updateOrdinanceDate,

        Boolean active
) {}
