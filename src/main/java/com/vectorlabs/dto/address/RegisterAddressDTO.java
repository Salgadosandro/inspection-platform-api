package com.vectorlabs.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAddressDTO(

        @NotBlank @Size(max = 120)
        String street,

        @NotBlank @Size(max = 30)
        String number,

        @NotBlank @Size(max = 120)
        String district,

        @NotBlank @Size(max = 120)
        String city,

        @NotBlank @Size(max = 2)
        String state,

        @NotBlank @Size(max = 20)
        String zipCode,

        @Size(max = 120)
        String country,

        @Size(max = 200)
        String complement
) {}

