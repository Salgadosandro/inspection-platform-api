package com.vectorlabs.dto.appuser;

import com.vectorlabs.dto.address.RegisterAddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAppUserDTO(

        @NotBlank
        @Email
        @Size(max = 200)
        String email,

        @NotBlank
        @Size(min = 8, max = 120)
        String password,

        @Size(max = 150)
        String name,

        @Valid
        RegisterAddressDTO address

) {}
