package com.vectorlabs.model.bases;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Auditable {

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    protected UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        beforeSave(false);

        UUID currentUserId = resolveCurrentUserId().orElse(null);
        this.updatedBy = currentUserId;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        beforeSave(true);

        UUID currentUserId = resolveCurrentUserId().orElse(null);
        this.createdBy = currentUserId;
        this.updatedBy = currentUserId;
    }

    protected void beforeSave(boolean isNew) {
        // default: não faz nada
    }

    /**
     * Estratégia recomendada:
     * - JWT: subject (sub) = UUID do usuário -> converte para UUID
     * - Caso não seja JWT, retorna vazio (ou adapte se seu principal carregar UUID).
     */
    protected Optional<UUID> resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object principal = auth.getPrincipal();

        // Resource Server JWT: principal costuma ser Jwt (ou JwtAuthenticationToken.getToken())
        if (principal instanceof Jwt jwt) {
            return parseUuid(jwt.getSubject())
                    .or(() -> parseUuid(jwt.getClaimAsString("uid"))); // opcional
        }

        // Se em algum ponto você usar um principal que já guarda UUID:
        if (principal instanceof UUID uuid) return Optional.of(uuid);

        // Caso você crie um UserDetails custom que tenha getId():
        // if (principal instanceof MyUserDetails u) return Optional.of(u.getId());

        return Optional.empty();
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            if (value == null || value.isBlank()) return Optional.empty();
            return Optional.of(UUID.fromString(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
