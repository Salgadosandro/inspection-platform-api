package com.vectorlabs.service;
import com.vectorlabs.dto.location.AnswerLocationDTO;
import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.LocationMapper;
import com.vectorlabs.model.Location;
import com.vectorlabs.repository.LocationRepository;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.LocationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository repository;

    @Mock
    private LocationMapper mapper;

    @Mock
    private LocationValidator validator;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private LocationService service;

    // =======================
    // CREATE
    // =======================

    @Test
    void save_shouldValidateAndSave_whenAdmin() {
        UUID userId = UUID.randomUUID();
        Location entity = new Location();

        when(securityService.isAdmin()).thenReturn(true);
        when(repository.save(entity)).thenReturn(entity);

        Location out = service.save(userId, entity);

        assertSame(entity, out);
        verify(securityService).isAdmin();
        verify(validator).validateCreation(userId, entity, true);
        verify(repository).save(entity);
        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }

    @Test
    void save_shouldValidateAndSave_whenNotAdmin() {
        UUID userId = UUID.randomUUID();
        Location entity = new Location();

        when(securityService.isAdmin()).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);

        Location out = service.save(userId, entity);

        assertSame(entity, out);
        verify(securityService).isAdmin();
        verify(validator).validateCreation(userId, entity, false);
        verify(repository).save(entity);
        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }

    // =======================
    // READ
    // =======================

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.findById(userId, id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper, validator, securityService);
    }

    @Test
    void findById_shouldValidateAccessAndReturn_whenFound_admin() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Location entity = new Location();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(true);

        Location out = service.findById(userId, id);

        assertSame(entity, out);
        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(validator).validateAccess(userId, entity, true);
        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }

    @Test
    void findById_shouldValidateAccessAndReturn_whenFound_notAdmin() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Location entity = new Location();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);

        Location out = service.findById(userId, id);

        assertSame(entity, out);
        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(validator).validateAccess(userId, entity, false);
        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }

    // =======================
    // SEARCH
    // =======================

    @Test
    void search_shouldValidateSearch_buildPageable_andMapResults_defaultsWhenNulls() {
        UUID userId = UUID.randomUUID();

        when(securityService.isAdmin()).thenReturn(false);

        // repo returns one entity
        Location entity = new Location();
        Page<Location> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(repository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Location>>any(),
                ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        AnswerLocationDTO dto = mock(AnswerLocationDTO.class);
        when(mapper.toDTO(entity)).thenReturn(dto);

        Page<AnswerLocationDTO> out = service.search(
                userId,
                UUID.randomUUID(),
                null, null, null, null, null, null, null,
                null, null
        );

        assertNotNull(out);
        assertEquals(1, out.getTotalElements());
        assertSame(dto, out.getContent().get(0));

        verify(securityService).isAdmin();
        verify(validator).validateSearch(eq(userId), eq(false), any(UUID.class));

        // pageable default (0,10)
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Location>>any(),
                pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        verify(mapper).toDTO(entity);

        verifyNoMoreInteractions(repository, validator, securityService, mapper);
    }

    @Test
    void search_shouldUseProvidedPageAndPageSize() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        when(securityService.isAdmin()).thenReturn(true);

        Page<Location> empty = new PageImpl<>(List.of(), PageRequest.of(5, 20), 0);
        when(repository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Location>>any(),
                ArgumentMatchers.any(Pageable.class))).thenReturn(empty);

        Page<AnswerLocationDTO> out = service.search(
                userId,
                companyId,
                "n", "c", "d", "s", "city", "st", "zip",
                5, 20
        );

        assertNotNull(out);
        assertEquals(0, out.getTotalElements());

        verify(securityService).isAdmin();
        verify(validator).validateSearch(userId, true, companyId);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Location>>any(),
                pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(5, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }

    // =======================
    // UPDATE
    // =======================

    @Test
    void update_shouldThrow_whenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UpdateLocationDTO dto = mock(UpdateLocationDTO.class);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.update(userId, id, dto));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper, validator, securityService);
    }

    @Test
    void update_shouldValidateAccess_validateUpdate_patch_save_andReturnDTO() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Location entity = new Location();
        UpdateLocationDTO dto = mock(UpdateLocationDTO.class);
        AnswerLocationDTO outDto = mock(AnswerLocationDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(outDto);

        AnswerLocationDTO out = service.update(userId, id, dto);

        assertSame(outDto, out);

        verify(repository).findById(id);

        // isAdmin() é chamado 2x no service (uma vez para validateAccess e outra para validateUpdate)
        verify(securityService, times(2)).isAdmin();
        verify(validator).validateAccess(userId, entity, false);
        verify(validator).validateUpdate(userId, dto, false);

        verify(mapper).updateFromDTO(dto, entity);
        verify(repository).save(entity);
        verify(mapper).toDTO(entity);

        verifyNoMoreInteractions(repository, validator, securityService, mapper);
    }

    // =======================
    // DELETE
    // =======================

    @Test
    void delete_shouldThrow_whenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.delete(userId, id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper, validator, securityService);
    }

    @Test
    void delete_shouldValidateAccess_andDeleteEntity() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Location entity = new Location();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(securityService.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> service.delete(userId, id));

        verify(repository).findById(id);
        verify(securityService).isAdmin();
        verify(validator).validateAccess(userId, entity, true);
        verify(repository).delete(entity);

        verifyNoMoreInteractions(repository, validator, securityService);
        verifyNoInteractions(mapper);
    }
}
