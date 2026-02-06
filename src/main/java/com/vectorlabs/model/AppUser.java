package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "app_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_user_email", columnNames = "email"),
                @UniqueConstraint(
                        name = "uk_app_user_provider",
                        columnNames = {"auth_provider", "provider_user_id"}
                ),
                @UniqueConstraint(name = "uk_app_user_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_app_user_cnpj", columnNames = "cnpj")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AppUser extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(length = 120)
    private String password; // null para OAuth2-only

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_user_id", length = 120)
    private String providerUserId;

    @Column(length = 150)
    private String name;

    @Column(length = 500)
    private String pictureUrl;

    private Boolean emailVerified;

    private Instant lastLoginAt;

    @Column(length = 14, unique = true)
    private String cpf;

    @Column(length = 18, unique = true)
    private String cnpj;

    @Embedded
    private Address address;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 30)
    private Set<UserRole> roles = new HashSet<>();

    @Override
    protected void beforeSave(boolean isNew) {
        if (isNew) {
            if (authProvider == null) authProvider = AuthProvider.LOCAL;
        }
    }
}

