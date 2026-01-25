package com.vectorlabs.dto.rule.ruleitem;

import java.time.Instant;
import java.util.UUID;

/**
 * Answer DTO
 */
public record AnswerRuleItemDTO(
        UUID id,
        String itemCode,
        String description,
        Integer itemSequence,
        Boolean active,
        Instant createdAt,
        Instant updatedAt,
        UUID moduleId,
        String moduleCode,
        UUID parentId
) {
}
