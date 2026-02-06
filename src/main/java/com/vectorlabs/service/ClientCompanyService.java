package com.vectorlabs.service;

import com.vectorlabs.dto.clientcompany.*;
import com.vectorlabs.exception.ObjectNotFound; // use a sua exception do projeto
import com.vectorlabs.mapper.ClientCompanyMapper;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.ClientCompanyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.vectorlabs.repository.specs.ClientCompanySpecs.*;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientCompanyService {

    private final ClientCompanyRepository repository;
    private final ClientCompanyMapper mapper;

    private final ClientCompanyValidator validations;
    private final SecurityService securityService;

    // =================== CREATE ===================

    @Transactional
    public AnswerClientCompanyDTO create(RegisterClientCompanyDTO dto) {
        validations.validateCreate(dto);

        var entity = mapper.fromRegisterDTO(dto);

        // USER sempre cria para si (admin também pode usar esse endpoint, mas cria para si)
        entity.setUser(securityService.getLoggedUser());

        // defaults de status (protege builder + payload)
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setDeletedAt(null);

        var saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public AnswerClientCompanyDTO adminCreate(RegisterClientCompanyAdminDTO dto) {
        validations.validateAdminCreate(dto);

        // garante que é admin mesmo (camada extra além do controller)
        if (!securityService.isAdmin()) {
            throw new AccessDeniedException("Admin privileges required.");
        }

        var entity = mapper.fromRegisterAdminDTO(dto); // vou te mostrar abaixo o ajuste do mapper

        // aqui você decide a fonte do user: pelo userId do DTO
        AppUser owner = validations.resolveOwner(dto.userId()); // ou appUserService.findById(...)
        entity.setUser(owner);

        entity.setActive(true);
        entity.setDeleted(false);
        entity.setDeletedAt(null);

        var saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    // =================== READ ===================

    @Transactional(readOnly = true)
    public AnswerClientCompanyDTO getDetails(UUID id) {
        var entity = findByIdSecured(id);
        return mapper.toDTO(entity);
    }


    @Transactional(readOnly = true)
    public Page<AnswerClientCompanyDTO> search(
            UUID requestedUserId,
            String corporateName,
            String tradeName,
            String cnpj,
            String phone,
            String email,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {
        validations.validateSearch(page, pageSize);

        UUID effectiveUserId = resolveEffectiveUserId(requestedUserId);

        var spec = Specification.allOf(
                notDeleted(),
                byUserId(effectiveUserId),
                corporateNameContains(corporateName),
                tradeNameContains(tradeName),
                eqCnpj(cnpj),
                phoneContains(phone),
                emailContains(email),
                isActive(active)
        );

        var pageable = PageRequest.of(page, pageSize);

        return repository.findAll(spec, pageable)
                .map(mapper::toDTO);
    }


    // =================== UPDATE ===================

    @Transactional
    public AnswerClientCompanyDTO update(UUID id, UpdateClientCompanyDTO dto) {
        validations.validateUpdate(dto);

        var entity = findByIdSecured(id);

        // regra: não atualiza se deletado
        validations.validateNotDeleted(entity);

        // patch (ignora nulls) + patch address correto
        mapper.updateFromDTO(dto, entity);

        var saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    // =================== SOFT DELETE ===================

    @Transactional
    public void softDelete(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found: " + id));

        // se já deletou, ok (idempotente)
        if (Boolean.TRUE.equals(entity.getDeleted())) return;

        enforceOwnershipOrAdmin(entity);

        validations.validateSoftDelete(entity);

        entity.setDeleted(true);
        entity.setActive(false);
        entity.setDeletedAt(Instant.now());

        repository.save(entity);
    }

    // =================== ACTIVATE / DEACTIVATE (opcional) ===================

    @Transactional
    public void activate(UUID id) {
        var entity = findByIdSecured(id);
        validations.validateNotDeleted(entity);

        entity.setActive(true);
        repository.save(entity);
    }

    @Transactional
    public void deactivate(UUID id) {
        var entity = findByIdSecured(id);
        validations.validateNotDeleted(entity);

        entity.setActive(false);
        repository.save(entity);
    }

    // =================== INTERNALS ===================

    private ClientCompany findByIdSecured(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found: " + id));

        // “deletado” deve se comportar como not found
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ObjectNotFound("ClientCompany not found: " + id);
        }

        enforceOwnershipOrAdmin(entity);
        return entity;
    }

    private UUID resolveEffectiveUserId(UUID requestedUserId) {
        if (securityService.isAdmin()) {
            return requestedUserId; // admin pode passar null (listar tudo) ou filtrar
        }
        return securityService.getLoggedUser().getId(); // user comum: sempre próprio
    }

    private void enforceOwnershipOrAdmin(ClientCompany company) {
        if (securityService.isAdmin()) return;

        AppUser logged = securityService.getLoggedUser();
        UUID ownerId = company.getUser().getId();

        if (!logged.getId().equals(ownerId)) {
            throw new AccessDeniedException("You cannot access this client company.");
        }
    }
}
