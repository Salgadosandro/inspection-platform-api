package com.vectorlabs.dto.rule.rulemodule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRuleModuleDTO(

        @NotNull
        UUID sectionId,

        @NotBlank
        @Size(max = 50)
        String moduleCode,

        @NotBlank
        @Size(max = 300)
        String moduleName,

        @NotNull
        Integer moduleSequence,

        Boolean active
) {}
