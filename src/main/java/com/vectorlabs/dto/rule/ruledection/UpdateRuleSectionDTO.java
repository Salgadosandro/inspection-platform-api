package com.vectorlabs.dto.rule.ruledection;
import jakarta.validation.constraints.Size;
public record UpdateRuleSectionDTO(

        @Size(max = 30)
        String code,
        @Size(max = 400)
        String name,
        Integer sequence,
        Boolean active
) {}
