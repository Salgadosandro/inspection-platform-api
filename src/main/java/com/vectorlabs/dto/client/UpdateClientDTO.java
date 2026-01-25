package com.vectorlabs.dto.client;

import jakarta.validation.constraints.Size;

public record UpdateClientDTO(

        @Size(max = 300)
        String clientSecret, // opcional: se vier, troca

        @Size(max = 500)
        String redirectUri,

        @Size(max = 500)
        String scope,

        Boolean enabled
) {}
