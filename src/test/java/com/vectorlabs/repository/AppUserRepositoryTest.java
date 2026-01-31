package com.vectorlabs.repository;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository repo;

    private AppUser newUser(String email) {
        AppUser u = new AppUser();

        // ===== ADAPTAR AQUI (de acordo com seu AppUser) =====
        u.setEmail(email);
        // Se você tiver normalizedEmail, use também:
        // u.setNormalizedEmail(email.toLowerCase());
        // u.setAuthProvider(AuthProvider.LOCAL);
        // u.setProviderUserId(null);
        // u.setCpf(null);
        // u.setCnpj(null);
        // =====================================================

        return u;
    }

    @Test
    void findByEmailIgnoreCase_shouldFind() {
        repo.save(newUser("Sandro@Email.com"));

        Optional<AppUser> found = repo.findByEmailIgnoreCase("sandro@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("Sandro@Email.com");
    }

    @Test
    void existsByEmailIgnoreCase_shouldBeTrue() {
        repo.save(newUser("Sandro@Email.com"));

        boolean exists = repo.existsByEmailIgnoreCase("sandro@email.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_shouldBeFalse() {
        repo.save(newUser("Sandro@Email.com"));

        boolean exists = repo.existsByEmailIgnoreCase("other@email.com");

        assertThat(exists).isFalse();
    }

    @Test
    void cpf_queries_shouldWork() {
        AppUser u = newUser("cpf@email.com");

        // ===== ADAPTAR AQUI =====
        u.setCpf("12345678901");
        // ========================

        repo.save(u);

        assertThat(repo.existsByCpf("12345678901")).isTrue();
        assertThat(repo.findByCpf("12345678901")).isPresent();
        assertThat(repo.findByCpf("00000000000")).isEmpty();
    }

    @Test
    void cnpj_queries_shouldWork() {
        AppUser u = newUser("cnpj@email.com");

        // ===== ADAPTAR AQUI =====
        u.setCnpj("12345678000199");
        // ========================

        repo.save(u);

        assertThat(repo.existsByCnpj("12345678000199")).isTrue();
        assertThat(repo.findByCnpj("12345678000199")).isPresent();
        assertThat(repo.findByCnpj("00000000000000")).isEmpty();
    }

    @Test
    void findByAuthProviderAndProviderUserId_shouldWork() {
        AppUser u = newUser("social@email.com");

        // ===== ADAPTAR AQUI =====
        u.setAuthProvider(AuthProvider.GOOGLE);
        u.setProviderUserId("google-123");
        // ========================

        repo.save(u);

        assertThat(repo.findByAuthProviderAndProviderUserId(AuthProvider.GOOGLE, "google-123")).isPresent();
        assertThat(repo.findByAuthProviderAndProviderUserId(AuthProvider.GITHUB, "google-123")).isEmpty();
        assertThat(repo.findByAuthProviderAndProviderUserId(AuthProvider.GOOGLE, "other")).isEmpty();
    }

    @Test
    void findByEmail_shouldBeExactMatch() {
        repo.save(newUser("Sandro@Email.com"));

        // Esse teste serve pra você enxergar o comportamento do seu banco.
        // Em alguns bancos/collation, isso pode ou não achar.
        assertThat(repo.findByEmail("sandro@email.com")).isEmpty();
        assertThat(repo.findByEmail("Sandro@Email.com")).isPresent();
    }
}
