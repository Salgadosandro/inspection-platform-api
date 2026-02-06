package com.vectorlabs.validator;

import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppUserValidator {

    private final AppUserRepository repository;

    public void requireId(UUID id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
    }
    public String normalizeEmail(String value) {
        if (value == null) return "";
        return Objects.toString(value, "").trim().toLowerCase();
    }
    public String normalizeEmailNullable(String value) {
        if (value == null) return null;
        String e = value.trim().toLowerCase();
        return e.isBlank() ? null : e;
    }
    public void validateRegister(String normalizedEmail) {
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("email already in use");
        }
    }
    public void validateAdminUpdate(UpdateAppUserDTO dto, AppUser current) {
        // lugar perfeito pra regra tipo: "não deixar remover o último ADMIN"
        // ou "não deixar desativar super-admin"
    }
    public void validateMeUpdate(UpdateAppUserDTO dto) {
        //Regras futuras
    }
    public void ensureNotDeletedForRead(AppUser user) {
        if (user.getDeleted()) {
            throw new IllegalArgumentException("user is deleted");
        }
    }
    public void ensureNotDeletedForWrite(AppUser user) {
        if (user.getDeleted()) {
            throw new IllegalArgumentException("user is deleted");
        }
    }
    public void ensureCanHardDelete(AppUser user) {
        // política: hard delete só se já foi soft-deletado
        if (!user.getDeleted()) {
            throw new IllegalArgumentException("hard delete only allowed after soft delete");
        }
    }
}
