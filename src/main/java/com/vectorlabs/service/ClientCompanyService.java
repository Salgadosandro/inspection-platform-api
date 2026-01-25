package com.vectorlabs.service;

import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.exception.ForbiddenAcessException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.ClientCompanyMapper;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.repository.specs.ClientCompanySpecs;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.ClientCompanyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientCompanyService {

    private final ClientCompanyRepository repository;
    private final ClientCompanyMapper mapper;
    private final ClientCompanyValidator validator;
    private final SecurityService securityService;

    // CREATE
    @Transactional
    public ClientCompany save(ClientCompany entity) {

        validator.validateCreation(entity);
        return repository.save(entity);
    }

    // READ - details
    @Transactional(readOnly = true)
    public ClientCompany findById(UUID id) {

        var found = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found: " + id));

        if (!found.getUserId().equals(securityService.getLoggedUser())) {
            throw new ForbiddenAcessException("Access denied to ClientCompany: " + id);
        }

        return found;
    }

    // READ - search (paged)
    @Transactional(readOnly = true)
    public Page<AnswerClientCompanyDTO> search(
            UUID userId,
            String name,
            String taxId,
            String phone,
            String email,
            Integer page,
            Integer pageSize
    ) {

        int p = (page == null || page < 0) ? 0 : page;
        int ps = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);

        UUID scopeUserId = (userId != null) ? userId : securityService.getLoggedUser().getId();
        Specification<ClientCompany> spec =
                (root, query, cb) -> cb.conjunction();

        spec = spec.and(ClientCompanySpecs.byUserId(scopeUserId))
                .and(ClientCompanySpecs.corporateNameContains(name))
                .and(ClientCompanySpecs.eqCnpj(taxId))
                .and(ClientCompanySpecs.phoneContains(phone))
                .and(ClientCompanySpecs.emailContains(email));

        var result = repository.findAll(spec, PageRequest.of(p, ps));
        return result.map(mapper::toDTO);
    }

    // UPDATE (PATCH semantics via UpdateDTO)
    @Transactional
    public AnswerClientCompanyDTO update(UUID id, UpdateClientCompanyDTO dto) {

        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found: " + id));

        if (!existing.getUserId().equals(securityService.getLoggedUser().getId())) {
            throw new ForbiddenAcessException("Access denied to ClientCompany: " + id);
        }

        validator.validateUpdate(existing, dto);
        mapper.updateFromDTO(dto, existing);

        var saved = repository.save(existing);
        return mapper.toDTO(saved);
    }

    // DELETE
    @Transactional
    public void delete(UUID id) {

        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found: " + id));

        if (!existing.getUserId().equals(securityService.getLoggedUser().getId())) {
            throw new ForbiddenAcessException("Access denied to ClientCompany: " + id);
        }

        validator.validateDelete(existing);
        repository.delete(existing);
    }
}
