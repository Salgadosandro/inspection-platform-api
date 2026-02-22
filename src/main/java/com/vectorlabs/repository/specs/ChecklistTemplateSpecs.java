package com.vectorlabs.repository.specs;

import com.vectorlabs.model.ChecklistTemplate;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class ChecklistTemplateSpecs {

    private ChecklistTemplateSpecs() {}

    // id = ?
    public static Specification<ChecklistTemplate> byId(UUID id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    // user.id = ?
    public static Specification<ChecklistTemplate> byUserId(UUID userId) {
        if (userId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.join("user", JoinType.LEFT).get("id"), userId);
    }

    // rule.id = ?
    public static Specification<ChecklistTemplate> byRuleId(UUID ruleId) {
        if (ruleId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.join("rule", JoinType.LEFT).get("id"), ruleId);
    }

    // title ILIKE %q%
    public static Specification<ChecklistTemplate> titleContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), like);
    }

    // description ILIKE %q%
    public static Specification<ChecklistTemplate> descriptionContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), like);
    }

    // active = ?
    public static Specification<ChecklistTemplate> eqActive(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("active"), active);
    }

    // rule.title ILIKE %q%
    public static Specification<ChecklistTemplate> ruleTitleContains(String q) {
        if (!notBlank(q)) return null;
        String like = likeWrap(q.toLowerCase());
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.join("rule", JoinType.LEFT).get("title")),
                        like
                );
    }

    // createdAt BETWEEN [from, to]
    public static Specification<ChecklistTemplate> createdBetween(Instant from, Instant to) {
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
    public static Specification<ChecklistTemplate> updatedBetween(Instant from, Instant to) {
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
    public static Specification<ChecklistTemplate> inIds(Collection<UUID> ids) {
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