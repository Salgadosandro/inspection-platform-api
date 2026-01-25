package com.vectorlabs.repository;

import com.vectorlabs.model.RuleSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RuleSectionRepository extends JpaRepository<RuleSection, UUID>, JpaSpecificationExecutor<RuleSection> {

    boolean existsByRule_IdAndSequence(UUID ruleId, Integer sequence);

    boolean existsByRule_IdAndSequenceAndIdNot(UUID ruleId, Integer seq, UUID id);

    boolean existsByRule_IdAndCode(UUID ruleId, String code);

    boolean existsByRule_IdAndCodeAndIdNot(UUID ruleId, String code, UUID id);
}
