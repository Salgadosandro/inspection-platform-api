package com.vectorlabs.repository;

import com.vectorlabs.model.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, UUID>, JpaSpecificationExecutor<ChecklistTemplate> {
}

