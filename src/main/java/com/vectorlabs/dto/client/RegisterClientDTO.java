package com.vectorlabs.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterClientDTO(

        @NotBlank
        @Size(max = 200)
        String clientId,

        @NotBlank
        @Size(max = 300)
        String clientSecret,

        @Size(max = 500)
        String redirectUri,

        @NotBlank
        @Size(max = 500)
        String scope
) {}
