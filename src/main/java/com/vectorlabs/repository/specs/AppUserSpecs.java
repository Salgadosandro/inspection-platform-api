package com.vectorlabs.repository.specs;

import com.vectorlabs.dto.appuser.SearchAppUserDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.validator.AppUserValidator;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class AppUserSpecs {

    private AppUserSpecs() {}

    // ----------------------------
    // Builder principal (SEM where)
    // ----------------------------
    public static Specification<AppUser> fromFilter(SearchAppUserDTO f, AppUserValidator validator) {
        if (f == null) return alwaysTrue();

        return andAll(
                idEquals(f.id()),
                nameLike(f.name()),
                emailLike(validator != null ? validator.normalizeEmailNullable(f.email()) : f.email()),
                cpfEquals(f.cpf()),
                cnpjEquals(f.cnpj()),

                // Address (city/state/country dentro de address)
                addressCityLike(f.city()),
                addressStateLike(f.state()),
                addressCountryLike(f.country()),

                hasEnabled(f.enabled()),
                hasDeleted(f.deleted()),
                hasAuthProvider(f.authProvider()),
                hasAnyRole(f.roles()),

                lastLoginBetween(f.lastLoginFrom(), f.lastLoginTo())
        );
    }

    // ----------------------------
    // Spec neutro (equivalente a "sempre verdadeiro")
    // ----------------------------
    public static Specification<AppUser> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    // ----------------------------
    // Specs simples
    // ----------------------------

    public static Specification<AppUser> idEquals(UUID id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<AppUser> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<AppUser> deleted() {
        return (root, query, cb) -> cb.isTrue(root.get("deleted"));
    }

    public static Specification<AppUser> hasDeleted(Boolean deleted) {
        if (deleted == null) return null;
        return deleted ? deleted() : notDeleted();
    }

    public static Specification<AppUser> hasEnabled(Boolean enabled) {
        if (enabled == null) return null;
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<AppUser> emailEquals(String email) {
        if (isBlank(email)) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("email")), email.trim().toLowerCase());
    }

    public static Specification<AppUser> emailLike(String emailPart) {
        if (isBlank(emailPart)) return null;
        String like = "%" + emailPart.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), like);
    }

    public static Specification<AppUser> nameLike(String namePart) {
        if (isBlank(namePart)) return null;
        String like = "%" + namePart.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    public static Specification<AppUser> cpfEquals(String cpf) {
        if (isBlank(cpf)) return null;
        return (root, query, cb) -> cb.equal(root.get("cpf"), cpf.trim());
    }

    public static Specification<AppUser> cnpjEquals(String cnpj) {
        if (isBlank(cnpj)) return null;
        return (root, query, cb) -> cb.equal(root.get("cnpj"), cnpj.trim());
    }

    public static Specification<AppUser> hasAuthProvider(AuthProvider provider) {
        if (provider == null) return null;
        return (root, query, cb) -> cb.equal(root.get("authProvider"), provider);
    }

    public static Specification<AppUser> providerUserIdEquals(String providerUserId) {
        if (isBlank(providerUserId)) return null;
        return (root, query, cb) -> cb.equal(root.get("providerUserId"), providerUserId.trim());
    }

    // ----------------------------
    // Roles
    // ----------------------------

    public static Specification<AppUser> hasRole(UserRole role) {
        if (role == null) return null;

        return (root, query, cb) -> {
            query.distinct(true);
            Join<AppUser, UserRole> rolesJoin = root.joinSet("roles", JoinType.INNER);
            return cb.equal(rolesJoin, role);
        };
    }

    public static Specification<AppUser> hasAnyRole(Collection<UserRole> roles) {
        if (roles == null || roles.isEmpty()) return null;

        return (root, query, cb) -> {
            query.distinct(true);
            Join<AppUser, UserRole> rolesJoin = root.joinSet("roles", JoinType.INNER);
            return rolesJoin.in(roles);
        };
    }

    // ----------------------------
    // Address (dentro de address)
    // ----------------------------

    public static Specification<AppUser> addressCityLike(String city) {
        return likeNested("address", "city", city);
    }

    public static Specification<AppUser> addressStateLike(String state) {
        return likeNested("address", "state", state);
    }

    public static Specification<AppUser> addressCountryLike(String country) {
        return likeNested("address", "country", country);
    }

    private static Specification<AppUser> likeNested(String parent, String field, String value) {
        if (isBlank(value)) return null;
        String like = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(parent).get(field)), like);
    }

    // ----------------------------
    // lastLogin range
    // ----------------------------

    public static Specification<AppUser> lastLoginBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;

        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("lastLogin"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("lastLogin"), from);
            }
            return cb.lessThanOrEqualTo(root.get("lastLogin"), to);
        };
    }

    // ----------------------------
    // Q (mantém disponível, mas NÃO chama no fromFilter
    // porque seu SearchAppUserDTO não tem q() no print)
    // ----------------------------

    public static Specification<AppUser> q(String q) {
        if (isBlank(q)) return null;

        String like = "%" + q.trim().toLowerCase() + "%";

        return (root, query, cb) -> {
            Expression<String> email = cb.lower(root.get("email"));
            Expression<String> name = cb.lower(root.get("name"));
            Expression<String> cpf = cb.lower(root.get("cpf"));
            Expression<String> cnpj = cb.lower(root.get("cnpj"));
            Expression<String> providerUserId = cb.lower(root.get("providerUserId"));

            return cb.or(
                    cb.like(email, like),
                    cb.like(name, like),
                    cb.like(cpf, like),
                    cb.like(cnpj, like),
                    cb.like(providerUserId, like)
            );
        };
    }

    // ----------------------------
    // AND helper (sem where)
    // ----------------------------
    @SafeVarargs
    public static Specification<AppUser> andAll(Specification<AppUser>... specs) {
        Specification<AppUser> result = alwaysTrue();
        if (specs == null) return result;

        for (Specification<AppUser> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
