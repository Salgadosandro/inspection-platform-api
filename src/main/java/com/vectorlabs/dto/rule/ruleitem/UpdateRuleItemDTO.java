package com.vectorlabs.dto.rule.ruleitem;



import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Update DTO (PATCH semantics)
 */
public record UpdateRuleItemDTO(

        @Size(max = 50)
        String itemCode,

        @Size(max = 4000)
        String description,

        Integer itemSequence,

        Boolean active,

        UUID parentId

) {
}
