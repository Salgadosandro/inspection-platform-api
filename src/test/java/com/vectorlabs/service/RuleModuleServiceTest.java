package com.vectorlabs.service;

import com.vectorlabs.dto.rule.rulemodule.AnswerRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleModuleMapper;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.validator.RuleModuleValidator;
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
class RuleModuleServiceTest {

    @Mock
    private RuleModuleRepository repository;

    @Mock
    private RuleModuleMapper mapper;

    @Mock
    private RuleModuleValidator validator;

    @InjectMocks
    private RuleModuleService service;

    private RuleModule entity;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        entity = new RuleModule();
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
        UUID sectionId = UUID.randomUUID();
        Instant now = Instant.now();

        // ⚠️ ajuste os campos/construtor abaixo caso seu record AnswerRuleModuleDTO tenha ordem diferente
        AnswerRuleModuleDTO dto = new AnswerRuleModuleDTO(
                id,
                ruleId,
                sectionId,
                "MOD-01",
                "Module Name",
                1,
                true,
                now,
                now
        );

        Page<RuleModule> page = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 10),
                1
        );

        when(repository.findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleModule>>any(),
                any(PageRequest.class)
        )).thenReturn(page);

        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.search(
                ruleId,
                sectionId,
                "MOD",
                "Module",
                1,
                true,
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(repository).findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleModule>>any(),
                any(PageRequest.class)
        );
        verify(mapper).toDTO(entity);
    }

    // ========================= UPDATE =========================

    @Test
    void update_shouldApplyPatchAndReturnDTO() {
        Instant now = Instant.now();
        UUID ruleId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();

        // ⚠️ ajuste a ordem/quantidade se seu record UpdateRuleModuleDTO for diferente
        UpdateRuleModuleDTO updateDTO = new UpdateRuleModuleDTO(
                "MOD-02",
                "Module Updated",
                2,
                false
        );

        // ⚠️ ajuste os campos/construtor conforme seu AnswerRuleModuleDTO real
        AnswerRuleModuleDTO outDTO = new AnswerRuleModuleDTO(
                id,
                ruleId,
                sectionId,
                "MOD-02",
                "Module Updated",
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

        UpdateRuleModuleDTO dto = new UpdateRuleModuleDTO(null, null, null, null);

        assertThrows(ObjectNotFound.class, () -> service.update(id, dto));

        verify(repository).findById(id);
        verifyNoMoreInteractions(mapper, validator);
        verify(repository, never()).save(any(RuleModule.class));
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
        verify(repository, never()).delete(any(RuleModule.class));
    }
}
