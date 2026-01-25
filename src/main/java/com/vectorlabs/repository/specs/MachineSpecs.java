package com.vectorlabs.repository.specs;

import com.vectorlabs.dto.machine.SearchMachineDTO;
import com.vectorlabs.model.Machine;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MachineSpecs {

    private MachineSpecs() {}

    /** Filtro por igualdade exata (case-insensitive) do tipo */
    public static Specification<Machine> hasType(String type) {
        return (root, query, cb) -> {
            String v = clean(type);
            if (isBlank(v)) return cb.conjunction();
            return cb.equal(cb.lower(root.get("type")), v.toLowerCase());
        };
    }

    /** Filtro por 'contém' (LIKE, case-insensitive) do tipo */
    public static Specification<Machine> typeLike(String type) {
        return (root, query, cb) -> {
            String v = clean(type);
            if (isBlank(v)) return cb.conjunction();
            return cb.like(cb.lower(root.get("type")), like(v));
        };
    }

    /** Filtro por igualdade exata (case-insensitive) do fabricante */
    public static Specification<Machine> hasManufacturer(String manufacturer) {
        return (root, query, cb) -> {
            String v = clean(manufacturer);
            if (isBlank(v)) return cb.conjunction();
            return cb.equal(cb.lower(root.get("manufacturer")), v.toLowerCase());
        };
    }

    /** Filtro por 'contém' (LIKE, case-insensitive) do fabricante */
    public static Specification<Machine> manufacturerLike(String manufacturer) {
        return (root, query, cb) -> {
            String v = clean(manufacturer);
            if (isBlank(v)) return cb.conjunction();
            return cb.like(cb.lower(root.get("manufacturer")), like(v));
        };
    }

    /** Filtro por igualdade exata (case-insensitive) do modelo */
    public static Specification<Machine> hasModel(String model) {
        return (root, query, cb) -> {
            String v = clean(model);
            if (isBlank(v)) return cb.conjunction();
            return cb.equal(cb.lower(root.get("model")), v.toLowerCase());
        };
    }

    /** Filtro por 'contém' (LIKE, case-insensitive) do modelo */
    public static Specification<Machine> modelLike(String model) {
        return (root, query, cb) -> {
            String v = clean(model);
            if (isBlank(v)) return cb.conjunction();
            return cb.like(cb.lower(root.get("model")), like(v));
        };
    }

    /**
     * Agregador de filtros SEM usar Specification.where:
     * Monta os predicados manualmente e retorna cb.and(...).
     * Aqui uso LIKE para todos os campos preenchidos do DTO.
     */
    public static Specification<Machine> withFilters(SearchMachineDTO dto) {
        return (root, query, cb) -> {
            if (dto == null) return cb.conjunction();

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            String type = clean(dto.getType());
            if (!isBlank(type)) {
                predicates.add(cb.like(cb.lower(root.get("type")), like(type)));
            }

            String manufacturer = clean(dto.getManufacturer());
            if (!isBlank(manufacturer)) {
                predicates.add(cb.like(cb.lower(root.get("manufacturer")), like(manufacturer)));
            }

            String model = clean(dto.getModel());
            if (!isBlank(model)) {
                predicates.add(cb.like(cb.lower(root.get("model")), like(model)));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction(); // nenhum filtro aplicado
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // ----------------- helpers -----------------

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String clean(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", " ");
    }

    private static String like(String s) {
        return "%" + s.toLowerCase() + "%";
    }
}
