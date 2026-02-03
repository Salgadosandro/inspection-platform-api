package com.vectorlabs.repository;

import com.vectorlabs.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<Machine, UUID>, JpaSpecificationExecutor<Machine> {

    boolean existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(String safe, String safe1, String safe2);
    boolean existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCaseAndIdNot(
            String type, String manufacturer, String model, UUID id);

}
