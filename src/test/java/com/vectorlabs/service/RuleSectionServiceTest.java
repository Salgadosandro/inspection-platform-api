package com.vectorlabs.service;

import com.vectorlabs.dto.rule.ruledection.AnswerRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleSectionMapper;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleSectionRepository;
import com.vectorlabs.validator.RuleSectionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleSectionServiceTest {

    @Mock
    private RuleSectionRepository repository;

    @Mock
    private RuleSectionMapper mapper;

    @Mock
    private RuleSectionValidator validator;

    @InjectMocks
    private RuleSectionService service;

    private RuleSection entity;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        entity = new RuleSection();
        entity.setId(id);
    }

    // ========================= CREATE =========================

    @Test
    void save_shouldValidateAndPersist() {
        when(repository.save(entity)).thenReturn(entity);

        var result = service.save(entity);

        assertNotNull(result);
        verify(validator).validateCreation(entity);
        verify(repository).save(entity);
    }

    // ========================= READ =========================

    @Test
    void findById_shouldReturnEntity_whenExists() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        var result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.findById(id));
        verify(repository).findById(id);
    }

    // ========================= SEARCH =========================

    @Test
    void search_shouldReturnPagedDTOs() {
        UUID ruleId = UUID.randomUUID();
        Instant now = Instant.now();

        AnswerRuleSectionDTO dto = new AnswerRuleSectionDTO(
                id,
                ruleId,
                "SEC-01",
                "Safety",
                1,
                true,
                now,
                now
        );

        Page<RuleSection> page = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 10),
                1
        );

        when(repository.findAll(
                any(Specification.class),
                any(PageRequest.class)
        )).thenReturn(page);

        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.search(
                ruleId,
                "SEC",
                "Safety",
                1,
                true,
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
        verify(mapper).toDTO(entity);
    }

    // ========================= UPDATE =========================

    @Test
    void update_shouldApplyPatchAndReturnDTO() {
        Instant now = Instant.now();
        UUID ruleId = UUID.randomUUID();

        UpdateRuleSectionDTO updateDTO = new UpdateRuleSectionDTO(
                "SEC-02",
                "Safety Updated",
                2,
                false
        );

        AnswerRuleSectionDTO outDTO = new AnswerRuleSectionDTO(
                id,
                ruleId,
                "SEC-02",
                "Safety Updated",
                2,
                false,
                now,
                now
        );

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(outDTO);

        var result = service.update(id, updateDTO);

        assertNotNull(result);

        verify(validator).validateUpdate(entity, updateDTO);
        verify(mapper).updateFromDTO(updateDTO, entity);
        verify(repository).save(entity);
        verify(mapper).toDTO(entity);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        UpdateRuleSectionDTO dto = new UpdateRuleSectionDTO(null, null, null, null);

        assertThrows(ObjectNotFound.class, () -> service.update(id, dto));
        verify(repository).findById(id);
        verifyNoMoreInteractions(mapper, validator);
    }

    // ========================= DELETE =========================

    @Test
    void delete_shouldValidateAndRemove() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.delete(id);

        verify(validator).validateDelete(entity);
        verify(repository).delete(entity);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.delete(id));

        verify(repository).findById(id);
        verifyNoMoreInteractions(mapper, validator);
        verify(repository, never()).delete(org.mockito.ArgumentMatchers.<RuleSection>any());

    }
}
