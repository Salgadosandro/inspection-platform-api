package com.vectorlabs.service;

import com.vectorlabs.dto.appuser.RegisterAppUserDTO;
import com.vectorlabs.dto.appuser.SearchAppUserDTO;
import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.AddressMapper;
import com.vectorlabs.model.Address;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.repository.specs.AppUserSpecs;
import com.vectorlabs.validator.AppUserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserValidator validator;
    private final AddressMapper addressMapper;

    @Transactional
    public AppUser register(RegisterAppUserDTO dto) {
        Objects.requireNonNull(dto, "RegisterAppUserDTO cannot be null");
        String email = validator.normalizeEmail(dto.email());
        validator.validateRegister(email);
        AppUser user = new AppUser();
        user.setName(dto.name());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setEnabled(true);
        user.setDeleted(false);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRoles(Set.of(UserRole.CLIENT));
        if (dto.address() != null) {
            Address address = addressMapper.toEntity(dto.address());
            user.setAddress(address);
        }
        return repository.save(user);
    }
    @Transactional(readOnly = true)
    public AppUser findById(UUID id) {
        validator.requireId(id);
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("AppUser not found: " + id));
        validator.ensureNotDeletedForRead(user);
        return user;
    }
    @Transactional
    public AppUser update(UUID id, UpdateAppUserDTO dto) {
        validator.requireId(id);
        Objects.requireNonNull(dto, "UpdateAppUserDTO cannot be null");
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("AppUser not found: " + id));
        validator.ensureNotDeletedForWrite(user);
        validator.validateAdminUpdate(dto, user);
        applyUpdate(user, dto);
        return repository.save(user);
    }

    @Transactional
    public AppUser updateMe(UpdateAppUserDTO dto, AppUser logged) {
        Objects.requireNonNull(dto, "UpdateAppUserDTO cannot be null");
        Objects.requireNonNull(logged, "Logged user cannot be null");
        AppUser user = repository.findById(logged.getId())
                .orElseThrow(() -> new ObjectNotFound("Logged AppUser not found: " + logged.getId()));
        validator.ensureNotDeletedForWrite(user);
        validator.validateMeUpdate(dto);
        applyUpdate(user, dto);
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<AppUser> search(SearchAppUserDTO filter, Pageable pageable) {
        Objects.requireNonNull(filter, "SearchAppUserDTO cannot be null");
        Objects.requireNonNull(pageable, "Pageable cannot be null");
        Specification<AppUser> spec = AppUserSpecs.fromFilter(filter, validator);
        return repository.findAll(spec, pageable);
    }

    @Transactional
    public AppUser patch(UUID id, UpdateAppUserDTO dto) {
        // Patch aqui reaproveita o update (e o controller deve proteger com @PreAuthorize)
        return update(id, dto);
    }

    @Transactional
    public void softDelete(UUID id) {
        validator.requireId(id);
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("AppUser not found: " + id));
        validator.ensureNotDeletedForWrite(user);
        user.setDeleted(true);
        user.setEnabled(false);
        repository.save(user);
    }

    @Transactional
    public AppUser deleteById(UUID id) {
        validator.requireId(id);
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("AppUser not found: " + id));
        validator.ensureCanHardDelete(user);
        repository.delete(user);
        return user;
    }

    @Transactional
    public AppUser softDeleteMe(AppUser logged) {
        Objects.requireNonNull(logged, "Logged user cannot be null");
        AppUser user = repository.findById(logged.getId())
                .orElseThrow(() -> new ObjectNotFound("Logged AppUser not found: " + logged.getId()));
        validator.ensureNotDeletedForWrite(user);
        user.setDeleted(true);
        user.setEnabled(false);
        return repository.save(user);
    }
    private void applyUpdate(AppUser user, UpdateAppUserDTO dto) {
        if (dto.name() != null) user.setName(dto.name());
        if (dto.address() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }
            Address addr = user.getAddress();
            if (dto.address().city() != null) {
                addr.setCity(dto.address().city());
            }
            if (dto.address().state() != null) {
                addr.setState(dto.address().state());
            }
            if (dto.address().country() != null) {
                addr.setCountry(dto.address().country());
            }
            // Se tiver mais campos:
            if (dto.address().zipCode() != null) addr.setZipCode(dto.address().zipCode());
            if (dto.address().street() != null)  addr.setStreet(dto.address().street());
            if (dto.address().number() != null)  addr.setNumber(dto.address().number());
            if (dto.address().district() != null) addr.setDistrict(dto.address().district());
        }
        if (dto.cpf() != null) {
            user.setCpf(dto.cpf());
        }
        if (dto.cnpj() != null) {
            user.setCnpj(dto.cnpj());
        }
    }
}
