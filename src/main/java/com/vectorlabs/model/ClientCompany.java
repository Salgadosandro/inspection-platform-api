package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "client_companies",
        indexes = {
                @Index(name = "idx_client_companies_user", columnList = "user_id"),
                @Index(name = "idx_client_companies_cnpj", columnList = "cnpj"),
                @Index(name = "idx_client_companies_active", columnList = "active"),
                @Index(name = "idx_client_companies_deleted", columnList = "deleted")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClientCompany extends Auditable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private AppUser user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street",      column = @Column(name = "address_street", length = 150)),
            @AttributeOverride(name = "number",      column = @Column(name = "address_number", length = 20)),
            @AttributeOverride(name = "complement",  column = @Column(name = "address_complement", length = 150)),
            @AttributeOverride(name = "district",    column = @Column(name = "address_district", length = 100)),
            @AttributeOverride(name = "city",        column = @Column(name = "address_city", length = 100)),
            @AttributeOverride(name = "state",       column = @Column(name = "address_state", length = 2)),
            @AttributeOverride(name = "zipCode",     column = @Column(name = "address_zip_code", length = 20)),
            @AttributeOverride(name = "country",     column = @Column(name = "address_country", length = 100))
    })
    private Address address;
    @Column(name = "corporate_name", nullable = false, length = 200)
    private String corporateName;

    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
