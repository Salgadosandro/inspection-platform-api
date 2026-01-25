package com.vectorlabs.repository.specs;

import com.vectorlabs.model.RuleModule;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class RuleModuleSpecs {

    private RuleModuleSpecs() {}

    /**
     * Filtra módulos pela versão da norma (Rule) via section.rule.id
     */
    public static Specification<RuleModule> byRuleId(UUID ruleId) {
        if (ruleId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("section").get("rule").get("id"), ruleId);
    }

    public static Specification<RuleModule> bySectionId(UUID sectionId) {
        if (sectionId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("section").get("id"), sectionId);
    }

    public static Specification<RuleModule> moduleCodeEq(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) return null;
        String v = moduleCode.trim();
        return (root, query, cb) -> cb.equal(root.get("moduleCode"), v);
    }

    public static Specification<RuleModule> moduleCodeContains(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) return null;
        String v = "%" + moduleCode.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("moduleCode")), v);
    }

    public static Specification<RuleModule> moduleNameContains(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) return null;
        String v = "%" + moduleName.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("moduleName")), v);
    }

    public static Specification<RuleModule> moduleSequenceEq(Integer moduleSequence) {
        if (moduleSequence == null) return null;
        return (root, query, cb) -> cb.equal(root.get("moduleSequence"), moduleSequence);
    }

    public static Specification<RuleModule> activeEq(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    /** Ordena por section.sequence asc, depois moduleSequence asc (bem útil pro front) */
    public static Specification<RuleModule> orderBySectionThenModuleSequenceAsc() {
        return (root, query, cb) -> {
            // garante join para ordenar por section.sequence
            var section = root.get("section");
            query.orderBy(
                    cb.asc(section.get("sequence")),
                    cb.asc(root.get("moduleSequence"))
            );
            return cb.conjunction();
        };
    }

    /** Se você quiser só moduleSequence asc (dentro de uma section) */
    public static Specification<RuleModule> orderByModuleSequenceAsc() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("moduleSequence")));
            return cb.conjunction();
        };
    }
}
