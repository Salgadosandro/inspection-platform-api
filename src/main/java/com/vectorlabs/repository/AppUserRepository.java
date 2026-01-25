package com.vectorlabs.repository;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByAuthProviderAndProviderUserId(AuthProvider provider, String s);

    boolean existsByEmailIgnoreCase(String normalizedEmail);

    boolean existsByCpf(@Size(max = 14) String cpf);

    boolean existsByCnpj(@Size(max = 18) String cnpj);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByCpf(String cpf);

    Optional<AppUser> findByCnpj(String cnpj);
}

