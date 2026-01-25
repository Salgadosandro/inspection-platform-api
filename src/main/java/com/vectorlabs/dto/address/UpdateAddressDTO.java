package com.vectorlabs.dto.address;

import jakarta.validation.constraints.Size;

public record UpdateAddressDTO(

        @Size(max = 120)
        String street,

        @Size(max = 30)
        String number,

        @Size(max = 120)
        String district,

        @Size(max = 120)
        String city,

        @Size(max = 2)
        String state,

        @Size(max = 20)
        String zipCode,

        @Size(max = 120)
        String country,

        @Size(max = 200)
        String complement
) {}
