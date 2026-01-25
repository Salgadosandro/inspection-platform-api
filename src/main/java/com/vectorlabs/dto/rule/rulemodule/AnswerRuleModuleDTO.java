package com.vectorlabs.dto.rule.rulemodule;


import java.time.Instant;
import java.util.UUID;

public record AnswerRuleModuleDTO(
        UUID id,
        UUID ruleId,
        UUID sectionId,
        String moduleCode,
        String moduleName,
        Integer moduleSequence,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
