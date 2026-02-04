package com.vectorlabs.service;
import static org.junit.jupiter.api.Assertions.*;
import com.vectorlabs.dto.rule.AnswerRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleMapper;
import com.vectorlabs.model.Rule;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.validator.RuleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock private RuleRepository repository;
    @Mock private RuleMapper mapper;
    @Mock private RuleValidator validator;

    @InjectMocks private RuleService service;

    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
    }

    // =========================
    // SAVE
    // =========================
    @Test
    void save_shouldValidateAndDefaultDeletedAndPersist() {
        Rule entity = new Rule();
        entity.setDeleted(null);

        Rule saved = new Rule();
        saved.setId(id);
        saved.setDeleted(false);

        doNothing().when(validator).validateCreation(entity);
        when(repository.save(entity)).thenReturn(saved);

        Rule result = service.save(entity);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(false, result.getDeleted());

        // default aplicado na entidade antes do save
        assertEquals(false, entity.getDeleted());

        verify(validator).validateCreation(entity);
        verify(repository).save(entity);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void save_shouldNotOverrideDeletedIfAlreadySet() {
        Rule entity = new Rule();
        entity.setDeleted(true);

        Rule saved = new Rule();
        saved.setId(id);
        saved.setDeleted(true);

        doNothing().when(validator).validateCreation(entity);
        when(repository.save(entity)).thenReturn(saved);

        Rule result = service.save(entity);

        assertEquals(true, entity.getDeleted());
        assertEquals(true, result.getDeleted());

        verify(validator).validateCreation(entity);
        verify(repository).save(entity);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    // =========================
    // FIND BY ID
    // =========================
    @Test
    void findById_shouldReturnRule_whenFound() {
        Rule found = new Rule();
        found.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(found));

        Rule result = service.findById(id);

        assertEquals(id, result.getId());
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void findById_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.findById(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    // =========================
    // SEARCH
    // =========================
    @Test
    void search_shouldUseDeletedFalseByDefault_whenDeletedParamIsNull() {
        // arrange
        Rule r1 = new Rule();
        Rule r2 = new Rule();

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "code"));
        Page<Rule> page = new PageImpl<>(List.of(r1, r2), expectedPageable, 2);

        AnswerRuleDTO dto1 = mock(AnswerRuleDTO.class);
        AnswerRuleDTO dto2 = mock(AnswerRuleDTO.class);

        // repository.findAll(spec, pageable)
        when(repository.findAll(any(Specification.class), eq(expectedPageable))).thenReturn(page);
        when(mapper.toDTO(r1)).thenReturn(dto1);
        when(mapper.toDTO(r2)).thenReturn(dto2);

        // act
        Page<AnswerRuleDTO> result = service.search(
                "NR",
                "Titulo",
                "Desc",
                true,
                null,   // <= importante: deleted null => deve filtrar FALSE
                0,
                10
        );

        // assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(repository).findAll(any(Specification.class), eq(expectedPageable));
        verify(mapper).toDTO(r1);
        verify(mapper).toDTO(r2);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void search_shouldAcceptDeletedTrue_whenProvided() {
        Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "code"));
        Page<Rule> empty = Page.empty(expectedPageable);

        when(repository.findAll(any(Specification.class), eq(expectedPageable))).thenReturn(empty);

        Page<AnswerRuleDTO> result = service.search(
                null, null, null,
                null,
                true, // deleted = true
                1,
                5
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(repository).findAll(any(Specification.class), eq(expectedPageable));
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    void update_shouldPatchAndSaveAndReturnDto() {
        UpdateRuleDTO dto = mock(UpdateRuleDTO.class);

        Rule existing = new Rule();
        existing.setId(id);

        Rule saved = new Rule();
        saved.setId(id);

        AnswerRuleDTO out = mock(AnswerRuleDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(validator).validateUpdate(existing, dto);
        // mapper.updateFromDTO(dto, existing) é void
        doNothing().when(mapper).updateFromDTO(dto, existing);
        when(repository.save(existing)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        AnswerRuleDTO result = service.update(id, dto);

        assertSame(out, result);

        verify(repository).findById(id);
        verify(validator).validateUpdate(existing, dto);
        verify(mapper).updateFromDTO(dto, existing);
        verify(repository).save(existing);
        verify(mapper).toDTO(saved);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void update_shouldThrowObjectNotFound_whenMissing() {
        UpdateRuleDTO dto = mock(UpdateRuleDTO.class);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.update(id, dto));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    // =========================
    // SOFT DELETE
    // =========================
    @Test
    void softDelete_shouldMarkDeletedAndInactiveAndSave() {
        Rule existing = new Rule();
        existing.setId(id);
        existing.setDeleted(false);
        existing.setActive(true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.softDelete(id);

        assertTrue(existing.getDeleted());
        assertFalse(existing.getActive());

        verify(repository).findById(id);
        verify(repository).save(existing);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void softDelete_shouldBeIdempotent_whenAlreadyDeleted() {
        Rule existing = new Rule();
        existing.setId(id);
        existing.setDeleted(true);
        existing.setActive(false);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.softDelete(id);

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void softDelete_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.softDelete(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    // =========================
    // RESTORE
    // =========================
    @Test
    void restore_shouldUnmarkDeletedAndSave_whenDeletedTrue() {
        Rule existing = new Rule();
        existing.setId(id);
        existing.setDeleted(true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.restore(id);

        assertFalse(existing.getDeleted());

        verify(repository).findById(id);
        verify(repository).save(existing);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void restore_shouldBeIdempotent_whenNotDeleted() {
        Rule existing = new Rule();
        existing.setId(id);
        existing.setDeleted(false);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.restore(id);

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }

    @Test
    void restore_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.restore(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, validator, mapper);
    }
}
