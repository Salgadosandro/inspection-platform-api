package com.vectorlabs.dto.client;

import java.time.Instant;
import java.util.UUID;

public record AnswerClientDTO(
        UUID id,
        String clientId,
        String scope,
        String redirectUri,
        Boolean enabled,
        Boolean deleted,
        Instant lastUsedAt
) {}
