package com.vectorlabs.service;

import com.vectorlabs.dto.location.AnswerLocationDTO;
import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.LocationMapper;
import com.vectorlabs.model.Location;
import com.vectorlabs.repository.LocationRepository;
import com.vectorlabs.repository.specs.LocationSpecs;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.LocationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository repository;
    private final LocationMapper mapper;
    private final LocationValidator validator;
    private final SecurityService securityService;

    // =======================
    // CREATE
    // =======================
    @Transactional
    public Location save(UUID userId, Location entity) {
        // regra de ownership: ADMIN pode tudo; user comum só se ClientCompany for dele
        validator.validateCreation(userId, entity, securityService.isAdmin());

        return repository.save(entity);
    }

    // =======================
    // READ
    // =======================
    @Transactional(readOnly = true)
    public Location findById(UUID userId, UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Location not found with id: " + id));

        validator.validateAccess(userId, entity, securityService.isAdmin());

        return entity;
    }

    // =======================
    // SEARCH
    // =======================
    @Transactional(readOnly = true)
    public Page<AnswerLocationDTO> search(
            UUID userId,
            UUID clientCompanyId,
            String name,
            String code,
            String description,
            String street,
            String city,
            String state,
            String zipCode,
            Integer page,
            Integer pageSize
    ) {
        // regra: se não for admin, restringir por userId (indiretamente via company ownership)
        validator.validateSearch(userId, securityService.isAdmin(), clientCompanyId);

        Specification<Location> spec = null;

        spec = and(spec, LocationSpecs.byClientCompanyId(clientCompanyId));
        spec = and(spec, LocationSpecs.nameContains(name));
        spec = and(spec, LocationSpecs.codeContains(code));
        spec = and(spec, LocationSpecs.descriptionContains(description));
        spec = and(spec, LocationSpecs.streetContains(street));
        spec = and(spec, LocationSpecs.cityContains(city));
        spec = and(spec, LocationSpecs.stateContains(state));
        spec = and(spec, LocationSpecs.zipCodeContains(zipCode));


        // Restrição de ownership: se não for admin, force clientCompanyId do usuário via validator/service (camada de regra)
        // Aqui a validação pode transformar/limitar o spec internamente se você preferir.
        // (Mantido simples; ownership deve ser aplicado no validator + specs adicionais se necessário.)

        var pageable = PageRequest.of(
                page != null ? page : 0,
                pageSize != null ? pageSize : 10
        );

        return repository.findAll(spec, pageable).map(mapper::toDTO);
    }

    // =======================
    // UPDATE
    // =======================
    @Transactional
    public AnswerLocationDTO update(UUID userId, UUID id, UpdateLocationDTO dto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Location not found with id: " + id));

        validator.validateAccess(userId, entity, securityService.isAdmin());
        validator.validateUpdate(userId, dto, securityService.isAdmin());

        mapper.updateFromDTO(dto, entity);

        var saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    // =======================
    // DELETE
    // =======================
    @Transactional
    public void delete(UUID userId, UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Location not found with id: " + id));

        validator.validateAccess(userId, entity, securityService.isAdmin());

        repository.delete(entity);
    }
    private static <T> Specification<T> and(Specification<T> base, Specification<T> next) {
        if (next == null) return base;
        return (base == null) ? next : base.and(next);
    }

}
