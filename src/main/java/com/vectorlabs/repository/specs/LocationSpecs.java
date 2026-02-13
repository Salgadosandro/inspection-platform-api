package com.vectorlabs.repository.specs;

import com.vectorlabs.model.Location;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class LocationSpecs {

    private LocationSpecs() {}

    // id = ?
    public static Specification<Location> byId(UUID id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    // company.id = ?
    public static Specification<Location> byClientCompanyId(UUID clientCompanyId) {
        if (clientCompanyId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.join("company", JoinType.LEFT).get("id"), clientCompanyId);
    }

    // name ILIKE %q%
    public static Specification<Location> nameContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    // code ILIKE %q%
    public static Specification<Location> codeContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("code")), like);
    }

    // description ILIKE %q%
    public static Specification<Location> descriptionContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), like);
    }

    // type = ?
    public static Specification<Location> eqType(Enum<?> type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    // createdAt BETWEEN [from, to]
    public static Specification<Location> createdBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null)              return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            else                           return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Location> updatedBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("updatedAt"), from, to);
            if (from != null)              return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            else                           return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }

    // address.street ILIKE %q%
    public static Specification<Location> streetContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("street")), like);
    }

    // address.city ILIKE %q%
    public static Specification<Location> cityContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("city")), like);
    }

    // address.state ILIKE %q%
    public static Specification<Location> stateContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("state")), like);
    }

    // address.zipCode ILIKE %q%
    public static Specification<Location> zipCodeContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("zipCode")), like);
    }

    // id IN (...)
    public static Specification<Location> inIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, query, cb) -> root.get("id").in(ids);
    }

    // ---------- helpers ----------
    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String likeWrap(String s) {
        return "%" + s.trim() + "%";
    }
}
