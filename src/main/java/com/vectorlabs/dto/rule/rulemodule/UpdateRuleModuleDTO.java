package com.vectorlabs.dto.rule.rulemodule;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateRuleModuleDTO(
        @Size(max = 50)
        String moduleCode,

        @Size(max = 300)
        String moduleName,

        Integer moduleSequence,

        Boolean active
) {}
