package com.vectorlabs.repository;

import com.vectorlabs.model.RuleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RuleItemRepository extends JpaRepository<RuleItem, UUID>, JpaSpecificationExecutor<RuleItem> {
    boolean existsByParent_Id(UUID id);

    boolean existsByModule_IdAndItemCode(UUID id, String itemCode);

    boolean existsByModule_IdAndItemCodeAndIdNot(UUID id, String effectiveCode, UUID id1);
}
