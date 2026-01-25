package com.vectorlabs.repository;

import com.vectorlabs.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}
