package com.vectorlabs.dto.rule.ruleitem;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Register DTO
 */
public record RegisterRuleItemDTO(

        @NotNull
        UUID moduleId,

        UUID parentId,

        @NotBlank
        @Size(max = 50)
        String itemCode,

        @NotBlank
        @Size(max = 4000)
        String description,

        @NotNull
        Integer itemSequence

) {
}
