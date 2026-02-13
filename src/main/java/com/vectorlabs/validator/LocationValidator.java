package com.vectorlabs.validator;

import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.model.Location;
import com.vectorlabs.repository.ClientCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocationValidator {

    private final ClientCompanyRepository clientCompanyRepository;

    // =======================
    // CREATE
    // =======================
    public void validateCreation(UUID userId, Location entity, boolean isAdmin) {
        if (entity == null) {
            throw new InvalidFieldException("Location entity cannot be null.");
        }

        if (entity.getCompany() == null) {
            throw new InvalidFieldException("ClientCompany is required.");
        }

        ClientCompany company = clientCompanyRepository.findById(entity.getCompany().getId())
                .orElseThrow(() ->
                        new ObjectNotFound("ClientCompany not found with id: " + entity.getCompany().getId())
                );

        if (!isAdmin) {
            validateOwnership(userId, company);
        }
    }

    // =======================
    // ACCESS (READ / UPDATE / DELETE)
    // =======================
    public void validateAccess(UUID userId, Location entity, boolean isAdmin) {
        if (entity == null) {
            throw new ObjectNotFound("Location not found.");
        }

        if (isAdmin) return;

        ClientCompany company = entity.getCompany();
        if (company == null) {
            throw new InvalidFieldException("Location has no associated ClientCompany.");
        }

        validateOwnership(userId, company);
    }

    // =======================
    // UPDATE
    // =======================
    public void validateUpdate(UUID userId, UpdateLocationDTO dto, boolean isAdmin) {
        if (dto == null) {
            throw new InvalidFieldException("UpdateLocationDTO cannot be null.");
        }

        // Aqui você pode colocar validações específicas de campos, se desejar.
        // Ownership já é validado em validateAccess().
    }

    // =======================
    // SEARCH
    // =======================
    public void validateSearch(UUID userId, boolean isAdmin, UUID clientCompanyId) {
        if (isAdmin) return;

        if (clientCompanyId == null) {
            throw new InvalidFieldException("clientCompanyId is required for non-admin users.");
        }

        ClientCompany company = clientCompanyRepository.findById(clientCompanyId)
                .orElseThrow(() ->
                        new ObjectNotFound("ClientCompany not found with id: " + clientCompanyId)
                );

        validateOwnership(userId, company);
    }

    // =======================
    // OWNERSHIP CHECK
    // =======================
    private void validateOwnership(UUID userId, ClientCompany company) {
        if (company.getUser() == null || company.getUser().getId() == null) {
            throw new InvalidFieldException("ClientCompany has no owner defined.");
        }

        if (!company.getUser().getId().equals(userId)) {
            throw new InvalidFieldException("You do not have permission to access this resource.");
        }
    }
}
