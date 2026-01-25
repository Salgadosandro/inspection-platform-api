package com.vectorlabs.dto.clientcompany;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterClientCompanyDTO(

        @NotNull
        UUID userId,

        UUID addressId,

        @NotBlank
        @Size(max = 200)
        String corporateName,

        @Size(max = 200)
        String tradeName,

        @Size(max = 18)
        String cnpj,

        @Size(max = 20)
        String phone,

        @Size(max = 150)
        String email
) {}
