package com.vectorlabs.service;

import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.ClientCompanyMapper;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.ClientCompanyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCompanyServiceTest {

    @Mock private ClientCompanyRepository repository;
    @Mock private ClientCompanyMapper mapper;
    @Mock private ClientCompanyValidator validations;
    @Mock private SecurityService securityService;

    @InjectMocks private ClientCompanyService service;

    // =================== CREATE ===================

    @Test
    void create_shouldSetDefaultsAndOwnerAndReturnDTO() {
        RegisterClientCompanyDTO dto = mock(RegisterClientCompanyDTO.class);

        AppUser logged = new AppUser();
        UUID loggedId = UUID.randomUUID();
        logged.setId(loggedId);

        ClientCompany entity = new ClientCompany();
        ClientCompany saved = new ClientCompany();
        AnswerClientCompanyDTO out = mock(AnswerClientCompanyDTO.class);

        when(mapper.fromRegisterDTO(dto)).thenReturn(entity);
        when(securityService.getLoggedUser()).thenReturn(logged);
        when(repository.save(any(ClientCompany.class))).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        AnswerClientCompanyDTO result = service.create(dto);

        assertSame(out, result);

        ArgumentCaptor<ClientCompany> captor = ArgumentCaptor.forClass(ClientCompany.class);
        verify(repository).save(captor.capture());
        ClientCompany toSave = captor.getValue();

        assertSame(logged, toSave.getUser());
        assertEquals(true, toSave.getActive());
        assertEquals(false, toSave.getDeleted());
        assertNull(toSave.getDeletedAt());

        verify(validations).validateCreate(dto);
        verify(mapper).fromRegisterDTO(dto);
        verify(securityService).getLoggedUser();
        verify(mapper).toDTO(saved);
        verifyNoMoreInteractions(repository, mapper, validations, securityService);
    }

    // =================== ADMIN CREATE ===================

    @Test
    void adminCreate_shouldThrow_whenNotAdmin() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class);

        when(securityService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.adminCreate(dto));

        verify(validations).validateAdminCreate(dto);
        verify(securityService).isAdmin();

        verifyNoInteractions(repository);
        verify(mapper, never()).fromRegisterAdminDTO(any());
        verify(validations, never()).resolveOwner(any());
        verifyNoMoreInteractions(validations, securityService);
    }

    @Test
    void adminCreate_shouldSetOwnerAndDefaults_whenAdmin() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class);
        UUID ownerId = UUID.randomUUID();
        when(dto.userId()).thenReturn(ownerId);

        when(securityService.isAdmin()).thenReturn(true);

        ClientCompany entity = new ClientCompany();
        ClientCompany saved = new ClientCompany();
        AnswerClientCompanyDTO out = mock(AnswerClientCompanyDTO.class);

        AppUser owner = new AppUser();
        owner.setId(ownerId);

        when(mapper.fromRegisterAdminDTO(dto)).thenReturn(entity);
        when(validations.resolveOwner(ownerId)).thenReturn(owner);
        when(repository.save(any(ClientCompany.class))).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        AnswerClientCompanyDTO result = service.adminCreate(dto);

        assertSame(out, result);

        ArgumentCaptor<ClientCompany> captor = ArgumentCaptor.forClass(ClientCompany.class);
        verify(repository).save(captor.capture());
        ClientCompany toSave = captor.getValue();

        assertSame(owner, toSave.getUser());
        assertEquals(true, toSave.getActive());
        assertEquals(false, toSave.getDeleted());
        assertNull(toSave.getDeletedAt());

        verify(validations).validateAdminCreate(dto);
        verify(securityService).isAdmin();
        verify(mapper).fromRegisterAdminDTO(dto);
        verify(validations).resolveOwner(ownerId);
        verify(mapper).toDTO(saved);
        verifyNoMoreInteractions(repository, mapper, validations, securityService);
    }

    // =================== GET DETAILS ===================

    @Test
    void getDetails_shouldReturnDTO_whenOwner() {
        UUID id = UUID.randomUUID();

        AppUser owner = new AppUser();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setUser(owner);

        AnswerClientCompanyDTO out = mock(AnswerClientCompanyDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(owner);
        when(mapper.toDTO(entity)).thenReturn(out);

        AnswerClientCompanyDTO result = service.getDetails(id);

        assertSame(out, result);

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(mapper).toDTO(entity);
        verifyNoMoreInteractions(repository, mapper, securityService);
        verifyNoInteractions(validations);
    }

    @Test
    void getDetails_shouldThrowObjectNotFound_whenDeleted() {
        UUID id = UUID.randomUUID();

        ClientCompany deleted = new ClientCompany();
        deleted.setId(id);
        deleted.setDeleted(true);

        when(repository.findById(id)).thenReturn(Optional.of(deleted));

        assertThrows(ObjectNotFound.class, () -> service.getDetails(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper, validations, securityService);
    }

    // =================== SEARCH ===================

    @Test
    void search_shouldUseLoggedUserId_whenNotAdmin() {
        UUID requestedUserId = UUID.randomUUID(); // deve ser ignorado se não admin
        int page = 0;
        int pageSize = 10;

        AppUser logged = new AppUser();
        UUID loggedId = UUID.randomUUID();
        logged.setId(loggedId);

        ClientCompany e1 = new ClientCompany();
        ClientCompany e2 = new ClientCompany();

        AnswerClientCompanyDTO d1 = mock(AnswerClientCompanyDTO.class);
        AnswerClientCompanyDTO d2 = mock(AnswerClientCompanyDTO.class);

        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(logged);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(e1, e2), PageRequest.of(page, pageSize), 2));

        when(mapper.toDTO(e1)).thenReturn(d1);
        when(mapper.toDTO(e2)).thenReturn(d2);

        Page<AnswerClientCompanyDTO> result = service.search(
                requestedUserId,
                null, null, null, null, null,
                null,
                page, pageSize
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertSame(d1, result.getContent().get(0));
        assertSame(d2, result.getContent().get(1));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertEquals(page, used.getPageNumber());
        assertEquals(pageSize, used.getPageSize());

        verify(validations).validateSearch(page, pageSize);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(mapper).toDTO(e1);
        verify(mapper).toDTO(e2);
        verifyNoMoreInteractions(repository, mapper, validations, securityService);
    }

    // =================== UPDATE ===================

    @Test
    void update_shouldPatchAndSave_whenAllowed() {
        UUID id = UUID.randomUUID();
        UpdateClientCompanyDTO dto = mock(UpdateClientCompanyDTO.class);

        AppUser owner = new AppUser();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setUser(owner);

        ClientCompany saved = new ClientCompany();
        AnswerClientCompanyDTO out = mock(AnswerClientCompanyDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(owner);

        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        AnswerClientCompanyDTO result = service.update(id, dto);

        assertSame(out, result);

        verify(validations).validateUpdate(dto);
        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(validations).validateNotDeleted(entity);

        verify(mapper).updateFromDTO(dto, entity);
        verify(repository).save(entity);
        verify(mapper).toDTO(saved);

        verifyNoMoreInteractions(repository, mapper, validations, securityService);
    }

    // =================== SOFT DELETE ===================

    @Test
    void softDelete_shouldBeIdempotent_whenAlreadyDeleted() {
        UUID id = UUID.randomUUID();

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(true);

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> service.softDelete(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(validations, mapper, securityService);
    }

    @Test
    void softDelete_shouldThrowAccessDenied_whenNotOwnerAndNotAdmin() {
        UUID id = UUID.randomUUID();

        AppUser owner = new AppUser();
        owner.setId(UUID.randomUUID());

        AppUser logged = new AppUser();
        logged.setId(UUID.randomUUID()); // diferente do owner

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setUser(owner);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(logged);

        assertThrows(AccessDeniedException.class, () -> service.softDelete(id));

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();

        verify(validations, never()).validateSoftDelete(any());
        verify(repository, never()).save(any());

        verifyNoMoreInteractions(repository, securityService);
        verifyNoInteractions(mapper);
    }

    @Test
    void softDelete_shouldMarkDeletedAndSave_whenOwner() {
        UUID id = UUID.randomUUID();

        AppUser owner = new AppUser();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setActive(true);
        entity.setUser(owner);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(owner);

        // validateSoftDelete não retorna nada, só garantir que é chamado
        doNothing().when(validations).validateSoftDelete(entity);

        assertDoesNotThrow(() -> service.softDelete(id));

        assertTrue(entity.getDeleted());
        assertFalse(entity.getActive());
        assertNotNull(entity.getDeletedAt());

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(validations).validateSoftDelete(entity);
        verify(repository).save(entity);

        verifyNoMoreInteractions(repository, validations, securityService);
        verifyNoInteractions(mapper);
    }

    // =================== ACTIVATE / DEACTIVATE ===================

    @Test
    void activate_shouldSetActiveTrue_andSave() {
        UUID id = UUID.randomUUID();

        AppUser owner = new AppUser();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setActive(false);
        entity.setUser(owner);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(owner);

        assertDoesNotThrow(() -> service.activate(id));

        assertTrue(entity.getActive());

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(validations).validateNotDeleted(entity);
        verify(repository).save(entity);

        verifyNoMoreInteractions(repository, validations, securityService);
        verifyNoInteractions(mapper);
    }

    @Test
    void deactivate_shouldSetActiveFalse_andSave() {
        UUID id = UUID.randomUUID();

        AppUser owner = new AppUser();
        UUID ownerId = UUID.randomUUID();
        owner.setId(ownerId);

        ClientCompany entity = new ClientCompany();
        entity.setId(id);
        entity.setDeleted(false);
        entity.setActive(true);
        entity.setUser(owner);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.getLoggedUser()).thenReturn(owner);

        assertDoesNotThrow(() -> service.deactivate(id));

        assertFalse(entity.getActive());

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(securityService).getLoggedUser();
        verify(validations).validateNotDeleted(entity);
        verify(repository).save(entity);

        verifyNoMoreInteractions(repository, validations, securityService);
        verifyNoInteractions(mapper);
    }
}
