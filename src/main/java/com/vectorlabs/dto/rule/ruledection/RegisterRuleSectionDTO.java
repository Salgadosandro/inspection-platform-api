package com.vectorlabs.dto.rule.ruledection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRuleSectionDTO(

        @NotNull
        UUID ruleId,

        @NotBlank
        @Size(max = 30)
        String code,

        @NotBlank
        @Size(max = 400)
        String name,

        @NotNull
        Integer sequence,

        Boolean active
) {}
