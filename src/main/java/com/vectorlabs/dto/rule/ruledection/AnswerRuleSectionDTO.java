package com.vectorlabs.dto.rule.ruledection;

import java.time.Instant;
import java.util.UUID;

public record AnswerRuleSectionDTO(
        UUID id,
        UUID ruleId,
        String code,
        String name,
        Integer sequence,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
