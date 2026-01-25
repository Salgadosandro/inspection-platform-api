package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(
        name = "client_companies",
        indexes = {
                @Index(name = "idx_client_companies_user", columnList = "user_id"),
                @Index(name = "idx_client_companies_cnpj", columnList = "cnpj")
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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "address_id")
    private UUID addressId;

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
}
