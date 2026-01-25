package com.vectorlabs.dto.auth;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(
        @NotBlank String refreshToken
) {}
