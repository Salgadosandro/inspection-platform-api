package com.vectorlabs.dto.location;

import com.vectorlabs.model.enuns.InspectionLocationType;
import jakarta.validation.constraints.Size;

public record UpdateLocationDTO(

        @Size(max = 150)
        String name,
        @Size(max = 50)
        String code,
        InspectionLocationType type,
        @Size(max = 2000)
        String description,
        @Size(max = 255)
        String street,
        @Size(max = 50)
        String number,
        @Size(max = 150)
        String neighborhood,

        @Size(max = 150)
        String city,

        @Size(max = 100)
        String state,

        @Size(max = 20)
        String zipCode,

        @Size(max = 100)
        String country
) {}
