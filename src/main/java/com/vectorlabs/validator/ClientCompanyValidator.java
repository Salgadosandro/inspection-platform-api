package com.vectorlabs.validator;

import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientCompanyValidator {
    private final AppUserService appUserService;

    public void validateCreate(RegisterClientCompanyDTO dto) {
        if (dto == null) {
            throw new MissingRequiredFieldException("ClientCompany payload cannot be null.");
        }

        if (isBlank(dto.corporateName())) {
            throw new MissingRequiredFieldException("Field 'corporateName' is required.");
        }

        // endereço: decide se é obrigatório
        if (dto.address() == null) {
            throw new MissingRequiredFieldException("Field 'address' is required.");
        }

        // aqui você NÃO valida user (vem do securityService)
    }

    public void validateAdminCreate(RegisterClientCompanyAdminDTO dto) {
        if (dto == null) {
            throw new MissingRequiredFieldException("ClientCompany payload cannot be null.");
        }

        if (dto.userId() == null) {
            throw new MissingRequiredFieldException("Field 'userId' is required for admin create.");
        }

        if (isBlank(dto.corporateName())) {
            throw new MissingRequiredFieldException("Field 'corporateName' is required.");
        }

        if (dto.address() == null) {
            throw new MissingRequiredFieldException("Field 'address' is required.");
        }
    }

    // =================== UPDATE ===================

    public void validateUpdate(UpdateClientCompanyDTO dto) {
        if (dto == null) {
            throw new MissingRequiredFieldException("Update payload cannot be null.");
        }

        // update é PATCH-like: tudo opcional
        // mas se vier vazio demais, você pode bloquear (opcional)
        if (
                isBlank(dto.corporateName()) &&
                        isBlank(dto.tradeName()) &&
                        isBlank(dto.cnpj()) &&
                        isBlank(dto.phone()) &&
                        isBlank(dto.email()) &&
                        dto.address() == null
        ) {
            throw new InvalidFieldException("At least one field must be provided for update.");
        }
    }

    // =================== SOFT DELETE ===================

    public void validateSoftDelete(ClientCompany company) {
        if (company == null) {
            throw new MissingRequiredFieldException("ClientCompany cannot be null.");
        }

        if (Boolean.TRUE.equals(company.getDeleted())) {
            throw new InvalidFieldException("ClientCompany is already deleted.");
        }
    }

    public void validateNotDeleted(ClientCompany company) {
        if (company == null) {
            throw new MissingRequiredFieldException("ClientCompany cannot be null.");
        }

        if (Boolean.TRUE.equals(company.getDeleted())) {
            throw new InvalidFieldException("Operation not allowed on deleted ClientCompany.");
        }
    }

    // =================== SEARCH ===================

    public void validateSearch(Integer page, Integer pageSize) {
        if (page != null && page < 0) {
            throw new InvalidFieldException("Page index cannot be negative.");
        }

        if (pageSize != null && pageSize <= 0) {
            throw new InvalidFieldException("Page size must be greater than zero.");
        }

        if (pageSize != null && pageSize > 100) {
            throw new InvalidFieldException("Page size max limit is 100.");
        }
    }
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    public AppUser resolveOwner(UUID userId) {
        if (userId == null) {
            throw new MissingRequiredFieldException("Field 'userId' is required.");
        }
        return appUserService.findById(userId);
    }
}
