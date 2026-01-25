package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rules_code",
                        columnNames = {"code"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Rule extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code; // ex: "NR-12"

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    // ✅ portaria vigente / última atualização oficial
    @Column(name = "update_ordinance", length = 200)
    private String updateOrdinance;
    // ex: "Portaria MTP nº 4.219, de 20/12/2022"

    // ✅ data oficial da portaria
    @Column(name = "update_ordinance_date")
    private LocalDate updateOrdinanceDate;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

}
