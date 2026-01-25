package com.vectorlabs.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 6, max = 200) String password
) {}
