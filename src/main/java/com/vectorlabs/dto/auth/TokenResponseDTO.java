package com.vectorlabs.dto.auth;

public record TokenResponseDTO(
        String tokenType,
        String accessToken,
        String refreshToken
) {}
