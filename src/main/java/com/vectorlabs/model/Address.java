package com.vectorlabs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Column(length = 150)
    private String street;

    @Column(length = 20)
    private String number;

    @Column(length = 150)
    private String complement;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state; // ex: RJ, SP, MG

    @Column(length = 20)
    private String zipCode;

    @Column(length = 100)
    private String country;
}
