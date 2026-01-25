package com.vectorlabs.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(
        name = "machines",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "manufacturer", "model"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** Tipo do equipamento (ex: Torno, Fresadora, Prensa) */
    @Column(length = 128, nullable = false)
    private String type;

    /** Fabricante (ex: Romi, Nardini, Bosch) */
    @Column(length = 255)
    private String manufacturer;

    /** Modelo específico (ex: KCTS Man 2500) */
    @Column(length = 255, nullable = false)
    private String model;
}
