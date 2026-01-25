package com.vectorlabs.repository.specs;

import com.vectorlabs.model.RuleItem;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class RuleItemSpecs {
    private RuleItemSpecs() {}
    // id = ?
    public static Specification<RuleItem> byId(UUID id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<RuleItem> any() {
        return (root, query, cb) -> cb.conjunction();
    }

    // module.id = ?
    public static Specification<RuleItem> byModuleId(UUID moduleId) {
        if (moduleId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.join("module", JoinType.LEFT).get("id"), moduleId);
    }
    // parent.id = ?
    public static Specification<RuleItem> byParentId(UUID parentId) {
        if (parentId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.join("parent", JoinType.LEFT).get("id"), parentId);
    }
    // itemCode ILIKE %q%
    public static Specification<RuleItem> itemCodeContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("itemCode")), like);
    }
    // description ILIKE %q%
    public static Specification<RuleItem> descriptionContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), like);
    }
    // itemSequence = ?
    public static Specification<RuleItem> itemSequenceEq(Integer seq) {
        if (seq == null) return null;
        return (root, query, cb) -> cb.equal(root.get("itemSequence"), seq);
    }
    // active = ?
    public static Specification<RuleItem> eqActive(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
    // createdAt BETWEEN [from, to]
    public static Specification<RuleItem> createdBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null)
                return cb.between(root.get("createdAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
    // updatedAt BETWEEN [from, to]
    public static Specification<RuleItem> updatedBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null)
                return cb.between(root.get("updatedAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }
    // id IN (...)
    public static Specification<RuleItem> inIds(Collection<UUID> ids) {
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
