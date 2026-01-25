package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "client",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_client_client_id", columnNames = "client_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Client extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false, length = 200)
    private String clientId;

    /**
     * Armazenar SEMPRE hash (BCrypt).
     * Nunca guardar secret em texto puro.
     */
    @Column(name = "client_secret_hash", nullable = false, length = 255)
    private String clientSecretHash;

    /**
     * Escopos em formato simples:
     * "nr12:read nr12:write inspections:read"
     */
    @Column(name = "scopes", nullable = false, length = 500)
    private String scopes;

    /**
     * Útil se você futuramente usar Authorization Code / Redirect
     * (para n8n normalmente não precisa, mas deixei porque você pediu).
     */
    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;

    private Instant lastUsedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @Override
    protected void beforeSave(boolean isNew) {
        // aqui você pode normalizar campos se quiser
    }
}

