package com.vectorlabs.dto.machine;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMachineDTO {

    private String type;

    private String manufacturer;

    private String model;
}
