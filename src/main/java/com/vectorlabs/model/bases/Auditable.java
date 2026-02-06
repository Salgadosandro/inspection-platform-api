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

    // =================== SOFT DELETE (PADRÃO) ===================

    @Column(name = "deleted", nullable = false)
    @lombok.Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    // =================== LIFECYCLE ===================

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

        // garante default do deleted no insert (protege builder)
        if (this.deleted == null) this.deleted = false;
    }

    // =================== API ÚTIL ===================

    public void softDelete() {
        if (Boolean.TRUE.equals(this.deleted)) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = resolveCurrentUserId().orElse(null);
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    protected void beforeSave(boolean isNew) {
        // default: não faz nada
    }

    protected Optional<UUID> resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return parseUuid(jwt.getSubject())
                    .or(() -> parseUuid(jwt.getClaimAsString("uid")));
        }

        if (principal instanceof UUID uuid) return Optional.of(uuid);

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
