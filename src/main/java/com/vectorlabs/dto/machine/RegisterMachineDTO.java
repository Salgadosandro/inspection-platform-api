package com.vectorlabs.dto.machine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMachineDTO {

    /** Tipo do equipamento (ex: Torno, Fresadora, Prensa) */
    @NotBlank(message = "Type is required.")
    @Size(max = 128, message = "Type must have a maximum of 128 characters.")
    private String type;

    /** Fabricante (ex: Romi, Nardini, Bosch) */
    @Size(max = 255, message = "Manufacturer must have a maximum of 255 characters.")
    private String manufacturer;

    /** Modelo específico (ex: KCTS Man 2500) */
    @NotBlank(message = "Model is required.")
    @Size(max = 255, message = "Model must have a maximum of 255 characters.")
    private String model;
}
