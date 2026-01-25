package com.vectorlabs.repository.specs;

import com.vectorlabs.model.Rule;
import org.springframework.data.jpa.domain.Specification;

public final class RuleSpecs {

    private RuleSpecs() {}

    // ---------- filtros individuais (NULL-SAFE) ----------

    public static Specification<Rule> codeLike(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.upper(root.get("code")),
                    "%" + code.trim().toUpperCase() + "%"
            );
        };
    }

    public static Specification<Rule> titleLike(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.upper(root.get("title")),
                    "%" + title.trim().toUpperCase() + "%"
            );
        };
    }

    public static Specification<Rule> descriptionLike(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.upper(root.get("description")),
                    "%" + description.trim().toUpperCase() + "%"
            );
        };
    }

    public static Specification<Rule> activeEquals(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<Rule> deletedEquals(Boolean deleted) {
        if (deleted == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("deleted"), deleted);
    }

    public static Specification<Rule> notDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }
}
