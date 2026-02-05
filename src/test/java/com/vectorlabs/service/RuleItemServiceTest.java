package com.vectorlabs.service;
import com.vectorlabs.dto.rule.ruleitem.AnswerRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleItemMapper;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.repository.RuleItemRepository;
import com.vectorlabs.validator.RuleItemValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleItemServiceTest {

    @Mock
    private RuleItemRepository repository;

    @Mock
    private RuleItemMapper mapper;

    @Mock
    private RuleItemValidator validator;

    @InjectMocks
    private RuleItemService service;

    private RuleItem entity;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        entity = new RuleItem();
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
        // não depende do construtor do record
        AnswerRuleItemDTO dto = mock(AnswerRuleItemDTO.class);

        Page<RuleItem> page = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 10),
                1
        );

        when(repository.findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleItem>>any(),
                any(PageRequest.class)
        )).thenReturn(page);

        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.search(
                UUID.randomUUID(),  // moduleId
                UUID.randomUUID(),  // parentId
                "ITM",              // itemCode
                "desc",             // description
                true,               // active
                0,                  // page
                10                  // pageSize
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));

        verify(repository).findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleItem>>any(),
                any(PageRequest.class)
        );
        verify(mapper).toDTO(entity);
    }

    @Test
    void search_shouldClampPageAndPageSize() {
        when(repository.findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleItem>>any(),
                any(PageRequest.class)
        )).thenReturn(Page.empty());

        // page < 0 => 0
        // pageSize > 100 => 100
        service.search(
                null,
                null,
                null,
                null,
                null,
                -5,
                1000
        );

        ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(repository).findAll(
                org.mockito.ArgumentMatchers.<Specification<RuleItem>>any(),
                prCaptor.capture()
        );

        PageRequest pr = prCaptor.getValue();
        assertEquals(0, pr.getPageNumber());
        assertEquals(100, pr.getPageSize());
    }

    // ========================= UPDATE =========================

    @Test
    void update_shouldApplyPatchAndReturnDTO() {
        UpdateRuleItemDTO updateDTO = mock(UpdateRuleItemDTO.class);
        AnswerRuleItemDTO outDTO = mock(AnswerRuleItemDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(outDTO);

        var result = service.update(id, updateDTO);

        assertNotNull(result);
        assertSame(outDTO, result);

        verify(validator).validateUpdate(entity, updateDTO);
        verify(mapper).updateFromDTO(updateDTO, entity);
        verify(repository).save(entity);
        verify(mapper).toDTO(entity);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        UpdateRuleItemDTO updateDTO = mock(UpdateRuleItemDTO.class);

        assertThrows(ObjectNotFound.class, () -> service.update(id, updateDTO));

        verify(repository).findById(id);
        verifyNoMoreInteractions(mapper, validator);
        verify(repository, never()).save(any(RuleItem.class));
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
        verify(repository, never()).delete(any(RuleItem.class));
    }
}
