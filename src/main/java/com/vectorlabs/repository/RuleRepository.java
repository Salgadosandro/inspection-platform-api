package com.vectorlabs.repository;

import com.vectorlabs.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID>, JpaSpecificationExecutor<Rule> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
