package com.vectorlabs.repository.specs;

import com.vectorlabs.model.RuleSection;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class RuleSectionSpecs {

    private RuleSectionSpecs() {}

    public static Specification<RuleSection> byRuleId(UUID ruleId) {
        if (ruleId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("rule").get("id"), ruleId);
    }

    public static Specification<RuleSection> codeEq(String code) {
        if (code == null || code.isBlank()) return null;
        String v = code.trim();
        return (root, query, cb) -> cb.equal(root.get("code"), v);
    }

    public static Specification<RuleSection> codeContains(String code) {
        if (code == null || code.isBlank()) return null;
        String v = "%" + code.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("code")), v);
    }

    public static Specification<RuleSection> nameContains(String name) {
        if (name == null || name.isBlank()) return null;
        String v = "%" + name.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), v);
    }

    public static Specification<RuleSection> sequenceEq(Integer sequence) {
        if (sequence == null) return null;
        return (root, query, cb) -> cb.equal(root.get("sequence"), sequence);
    }

    public static Specification<RuleSection> activeEq(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    /** Ordena por sequence asc (útil em listagens) */
    public static Specification<RuleSection> orderBySequenceAsc() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("sequence")));
            return cb.conjunction();
        };
    }
}
