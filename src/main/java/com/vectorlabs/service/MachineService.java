package com.vectorlabs.service;

import com.vectorlabs.dto.machine.SearchMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.MachineMapper;
import com.vectorlabs.model.Machine;
import com.vectorlabs.repository.MachineRepository;
import com.vectorlabs.repository.specs.MachineSpecs;
import com.vectorlabs.validator.MachineValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository repository;
    private final MachineMapper mapper;
    private final MachineValidator validator;

    // ------------------- CREATE -------------------
    @Transactional
    public Machine save(Machine entity) {
        normalize(entity);

        validator.validateCreation(entity);

        if (repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                safe(entity.getType()),
                safe(entity.getManufacturer()),
                safe(entity.getModel()))) {
            throw new DoubleRegisterException(
                    "A machine with the same type, manufacturer and model already exists."
            );
        }

        return repository.save(entity);
    }

    // ------------------- UPDATE -------------------
    @Transactional
    public Machine update(UUID id, UpdateMachineDTO dto) {
        Machine before = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Machine not found."));

        validator.validateUpdate(before, dto);

        mapper.updateEntity(before, dto);
        normalize(before);

        if (repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCaseAndIdNot(
                safe(before.getType()),
                safe(before.getManufacturer()),
                safe(before.getModel()),
                before.getId())) {
            throw new DoubleRegisterException(
                    "A machine with the same type, manufacturer and model already exists."
            );
        }

        return repository.save(before);
    }

    // ------------------- READ -------------------
    @Transactional(readOnly = true)
    public Machine findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Machine not found."));
    }

    // ------------------- DELETE -------------------
    @Transactional
    public void deleteById(UUID id) {
        Machine entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Machine not found."));

        validator.validateDelete(entity);

        repository.delete(entity);
    }

    // ------------------- SEARCH -------------------
    @Transactional(readOnly = true)
    public Page<Machine> search(SearchMachineDTO filters, Integer page, Integer pageSize) {
        int p  = (page == null || page < 0) ? 0 : page;
        int ps = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);

        Specification<Machine> spec = MachineSpecs.withFilters(filters);
        return repository.findAll(spec, PageRequest.of(p, ps));
    }

    // ------------------- HELPERS -------------------
    private void normalize(Machine m) {
        if (m == null) return;
        m.setType(clean(m.getType(), 128));
        m.setManufacturer(clean(m.getManufacturer(), 255));
        m.setModel(clean(m.getModel(), 255));
    }

    private String clean(String value, int max) {
        if (value == null) return null;
        String v = value.trim().replaceAll("\\s+", " ");
        return v.length() > max ? v.substring(0, max) : v;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
