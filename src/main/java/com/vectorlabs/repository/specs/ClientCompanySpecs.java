package com.vectorlabs.repository.specs;

import com.vectorlabs.model.ClientCompany;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class ClientCompanySpecs {

    private ClientCompanySpecs() {}

    // id = ?
    public static Specification<ClientCompany> byId(UUID id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    // user.id = ? (escopo multitenant)
    public static Specification<ClientCompany> byUserId(UUID userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    // deleted = false (soft delete)
    public static Specification<ClientCompany> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    // active = true
    public static Specification<ClientCompany> isActive(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    // corporateName ILIKE %q%
    public static Specification<ClientCompany> corporateNameContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("corporateName")), like);
    }

    // tradeName ILIKE %q%
    public static Specification<ClientCompany> tradeNameContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("tradeName")), like);
    }

    // cnpj = ?
    public static Specification<ClientCompany> eqCnpj(String cnpj) {
        if (!notBlank(cnpj)) return null;
        return (root, query, cb) -> cb.equal(root.get("cnpj"), cnpj.trim());
    }

    // phone ILIKE %q%
    public static Specification<ClientCompany> phoneContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("phone")), like);
    }

    // email ILIKE %q%
    public static Specification<ClientCompany> emailContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), like);
    }

    // createdAt BETWEEN [from, to]  (confirme se Auditable usa "createdAt")
    public static Specification<ClientCompany> createdBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    // updatedAt BETWEEN [from, to] (confirme se Auditable usa "updatedAt")
    public static Specification<ClientCompany> updatedBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("updatedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }

    // id IN (...)
    public static Specification<ClientCompany> inIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, query, cb) -> root.get("id").in(ids);
    }

    // (Opcional) filtros por campos do endereço embutido
    public static Specification<ClientCompany> addressCityEquals(String city) {
        if (!notBlank(city)) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("address").get("city")), city.trim().toLowerCase());
    }

    // ---------- helpers ----------
    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String likeWrap(String s) {
        return "%" + s.trim() + "%";
    }
}
