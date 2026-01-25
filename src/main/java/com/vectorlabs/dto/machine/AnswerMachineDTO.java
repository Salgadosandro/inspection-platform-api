package com.vectorlabs.dto.machine;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMachineDTO {

    private UUID id;

    private String type;

    private String manufacturer;

    private String model;
}