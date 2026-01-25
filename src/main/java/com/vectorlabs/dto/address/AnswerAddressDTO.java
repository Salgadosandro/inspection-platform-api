package com.vectorlabs.dto.address;
public record AnswerAddressDTO(

        String street,
        String number,
        String district,
        String city,
        String state,
        String zipCode,
        String country,
        String complement
) {}
