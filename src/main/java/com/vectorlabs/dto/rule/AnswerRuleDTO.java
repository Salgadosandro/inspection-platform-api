package com.vectorlabs.dto.rule;

import java.time.LocalDate;
import java.util.UUID;

public record AnswerRuleDTO(

        UUID id,

        String code,

        String title,

        String description,

        String updateOrdinance,
        // ex: "Portaria MTP nº 4.219, de 20/12/2022"

        LocalDate updateOrdinanceDate,

        Boolean active,
        Boolean deleted
) {}
