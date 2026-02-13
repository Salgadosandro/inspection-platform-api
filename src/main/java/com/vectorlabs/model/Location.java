package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import com.vectorlabs.model.enuns.InspectionLocationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(
        name = "locations",
        indexes = {
                @Index(name = "idx_locations_company", columnList = "company_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Location extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private ClientCompany company;
    @Embedded
    private Address address;
    @Column(name = "name", length = 150, nullable = false)
    private String name;
    @Column(name = "code", length = 50)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InspectionLocationType type;
    @Column(name = "description", columnDefinition = "text")
    private String description;
}
