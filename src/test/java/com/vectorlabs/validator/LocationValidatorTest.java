package com.vectorlabs.validator;

import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.model.Location;
import com.vectorlabs.repository.ClientCompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationValidatorTest {

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @InjectMocks
    private LocationValidator validator;

    // ======================================================
    // CREATE
    // ======================================================

    @Test
    void validateCreation_shouldThrow_whenEntityNull() {
        assertThrows(InvalidFieldException.class,
                () -> validator.validateCreation(UUID.randomUUID(), null, false));

        verifyNoInteractions(clientCompanyRepository);
    }

    @Test
    void validateCreation_shouldThrow_whenCompanyNull() {
        Location location = new Location();

        assertThrows(InvalidFieldException.class,
                () -> validator.validateCreation(UUID.randomUUID(), location, false));

        verifyNoInteractions(clientCompanyRepository);
    }

    @Test
    void validateCreation_shouldThrow_whenCompanyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        ClientCompany company = new ClientCompany();
        company.setId(companyId);

        Location location = new Location();
        location.setCompany(company);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class,
                () -> validator.validateCreation(userId, location, false));

        verify(clientCompanyRepository).findById(companyId);
    }

    @Test
    void validateCreation_shouldThrow_whenNotOwner_andNotAdmin() {
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(ownerId);

        ClientCompany company = new ClientCompany();
        company.setId(companyId);
        company.setUser(owner);

        Location location = new Location();
        location.setCompany(company);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        assertThrows(InvalidFieldException.class,
                () -> validator.validateCreation(userId, location, false));
    }

    @Test
    void validateCreation_shouldPass_whenAdmin() {
        UUID companyId = UUID.randomUUID();

        ClientCompany company = new ClientCompany();
        company.setId(companyId);

        Location location = new Location();
        location.setCompany(company);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        assertDoesNotThrow(() ->
                validator.validateCreation(UUID.randomUUID(), location, true));
    }

    @Test
    void validateCreation_shouldPass_whenOwner() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(userId);

        ClientCompany company = new ClientCompany();
        company.setId(companyId);
        company.setUser(owner);

        Location location = new Location();
        location.setCompany(company);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        assertDoesNotThrow(() ->
                validator.validateCreation(userId, location, false));
    }

    // ======================================================
    // ACCESS
    // ======================================================

    @Test
    void validateAccess_shouldThrow_whenLocationNull() {
        assertThrows(ObjectNotFound.class,
                () -> validator.validateAccess(UUID.randomUUID(), null, false));
    }

    @Test
    void validateAccess_shouldPass_whenAdmin() {
        Location location = new Location();
        assertDoesNotThrow(() ->
                validator.validateAccess(UUID.randomUUID(), location, true));
    }

    @Test
    void validateAccess_shouldThrow_whenCompanyNull() {
        Location location = new Location();

        assertThrows(InvalidFieldException.class,
                () -> validator.validateAccess(UUID.randomUUID(), location, false));
    }

    @Test
    void validateAccess_shouldThrow_whenNotOwner() {
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(ownerId);

        ClientCompany company = new ClientCompany();
        company.setUser(owner);

        Location location = new Location();
        location.setCompany(company);

        assertThrows(InvalidFieldException.class,
                () -> validator.validateAccess(userId, location, false));
    }

    @Test
    void validateAccess_shouldPass_whenOwner() {
        UUID userId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(userId);

        ClientCompany company = new ClientCompany();
        company.setUser(owner);

        Location location = new Location();
        location.setCompany(company);

        assertDoesNotThrow(() ->
                validator.validateAccess(userId, location, false));
    }

    // ======================================================
    // UPDATE
    // ======================================================

    @Test
    void validateUpdate_shouldThrow_whenDtoNull() {
        assertThrows(InvalidFieldException.class,
                () -> validator.validateUpdate(UUID.randomUUID(), null, false));
    }

    @Test
    void validateUpdate_shouldPass_whenValid() {
        UpdateLocationDTO dto = mock(UpdateLocationDTO.class);

        assertDoesNotThrow(() ->
                validator.validateUpdate(UUID.randomUUID(), dto, false));
    }

    // ======================================================
    // SEARCH
    // ======================================================

    @Test
    void validateSearch_shouldPass_whenAdmin() {
        assertDoesNotThrow(() ->
                validator.validateSearch(UUID.randomUUID(), true, null));
    }

    @Test
    void validateSearch_shouldThrow_whenNonAdminAndCompanyNull() {
        assertThrows(InvalidFieldException.class,
                () -> validator.validateSearch(UUID.randomUUID(), false, null));

        verifyNoInteractions(clientCompanyRepository);
    }

    @Test
    void validateSearch_shouldThrow_whenCompanyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class,
                () -> validator.validateSearch(userId, false, companyId));
    }

    @Test
    void validateSearch_shouldThrow_whenNotOwner() {
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(ownerId);

        ClientCompany company = new ClientCompany();
        company.setUser(owner);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        assertThrows(InvalidFieldException.class,
                () -> validator.validateSearch(userId, false, companyId));
    }

    @Test
    void validateSearch_shouldPass_whenOwner() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(userId);

        ClientCompany company = new ClientCompany();
        company.setUser(owner);

        when(clientCompanyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        assertDoesNotThrow(() ->
                validator.validateSearch(userId, false, companyId));
    }
}
