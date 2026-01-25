package com.vectorlabs.repository;

import com.vectorlabs.model.RuleModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RuleModuleRepository extends JpaRepository<RuleModule, UUID>, JpaSpecificationExecutor<RuleModule> {
    boolean existsBySection_IdAndModuleCode(UUID sectionId, String moduleCode);

    boolean existsBySection_IdAndModuleSequence(UUID sectionId, Integer moduleSequence);

    boolean existsBySection_IdAndModuleCodeAndIdNot(UUID sectionId, String moduleCode, UUID id);

    boolean existsBySection_IdAndModuleSequenceAndIdNot(UUID sectionId, Integer seq, UUID id);

    boolean existsBySection_Id(UUID id);
}

