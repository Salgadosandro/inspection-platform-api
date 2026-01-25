package com.vectorlabs.validator;

import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.exception.ForbiddenAcessException;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientCompanyValidator {

    private final ClientCompanyRepository repository;
    private final AppUserService userService;
    private final SecurityService securityService;

    // CREATE
    public void validateCreation(ClientCompany entity) {
        var user = securityService.getLoggedUser();

        // owner / escopo
        if (entity.getUserId() == null || !entity.getUserId().equals(user.getId())) {
            throw new ForbiddenAcessException("ClientCompany does not belong to the authenticated user.");
        }

        // campos obrigatórios
        if (!notBlank(entity.getCorporateName())) {
            throw new InvalidFieldException("corporateName is required.");
        }

        if (entity.getCorporateName().length() > 200) {
            throw new InvalidFieldException("corporateName max length is 200.");
        }

        if (entity.getTradeName() != null && entity.getTradeName().length() > 200) {
            throw new InvalidFieldException("tradeName max length is 200.");
        }

        if (entity.getCnpj() != null && entity.getCnpj().length() > 18) {
            throw new InvalidFieldException("cnpj max length is 18.");
        }

        if (entity.getPhone() != null && entity.getPhone().length() > 20) {
            throw new InvalidFieldException("phone max length is 20.");
        }

        if (entity.getEmail() != null && entity.getEmail().length() > 150) {
            throw new InvalidFieldException("email max length is 150.");
        }
    }

    // UPDATE (PATCH)
    public void validateUpdate(ClientCompany before, UpdateClientCompanyDTO dto) {
        var user = securityService.getLoggedUser();

        // owner / escopo
        assertOwnedByUser(before, user.getId());

        // validar apenas campos presentes
        if (dto.corporateName() != null) {
            if (!notBlank(dto.corporateName())) {
                throw new InvalidFieldException("corporateName cannot be blank.");
            }
            if (dto.corporateName().length() > 200) {
                throw new InvalidFieldException("corporateName max length is 200.");
            }
        }

        if (dto.tradeName() != null && dto.tradeName().length() > 200) {
            throw new InvalidFieldException("tradeName max length is 200.");
        }

        if (dto.cnpj() != null && dto.cnpj().length() > 18) {
            throw new InvalidFieldException("cnpj max length is 18.");
        }

        if (dto.phone() != null && dto.phone().length() > 20) {
            throw new InvalidFieldException("phone max length is 20.");
        }

        if (dto.email() != null && dto.email().length() > 150) {
            throw new InvalidFieldException("email max length is 150.");
        }
    }

    // DELETE
    public void validateDelete(ClientCompany entity) {
        var user = securityService.getLoggedUser();
        assertOwnedByUser(entity, user.getId());

        // dependências futuras podem ser validadas aqui
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void assertOwnedByUser(ClientCompany entity, UUID userId) {
        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new ForbiddenAcessException("ClientCompany does not belong to the authenticated user.");
        }
    }
}
