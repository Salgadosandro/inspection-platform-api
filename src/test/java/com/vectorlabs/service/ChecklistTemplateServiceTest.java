package com.vectorlabs.service;

import com.vectorlabs.dto.checklisttemplate.AnswerChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.ChecklistTemplateMapper;
import com.vectorlabs.model.ChecklistTemplate;
import com.vectorlabs.repository.ChecklistTemplateRepository;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.ChecklistTemplateValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários (Mockito) para ChecklistTemplateService.
 *
 * Nota: seu repository provavelmente estende JpaSpecificationExecutor e QueryByExampleExecutor,
 * então findOne(...) é overload (Specification vs Example). Por isso usamos matcher tipado
 * ArgumentMatchers.<Specification<ChecklistTemplate>>any() para evitar "Ambiguous method call".
 */
@ExtendWith(MockitoExtension.class)
class ChecklistTemplateServiceTest {

    @Mock private ChecklistTemplateRepository repository;
    @Mock private ChecklistTemplateMapper mapper;
    @Mock private SecurityService securityService;
    @Mock private ChecklistTemplateValidator validator;

    @InjectMocks private ChecklistTemplateService service;

    private static Specification<ChecklistTemplate> anySpec() {
        return ArgumentMatchers.<Specification<ChecklistTemplate>>any();
    }

    @Nested
    class Save {

        @Test
        @DisplayName("save: admin -> valida criação, seta defaults defensivos e salva")
        void save_admin_setsDefaults_andSaves() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = new ChecklistTemplate();

            when(securityService.isAdmin()).thenReturn(true);
            when(repository.save(any(ChecklistTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            ChecklistTemplate saved = service.save(loggedUserId, entity);

            assertNotNull(saved);
            assertFalse(saved.isDefault(), "default deve ser false");
            assertTrue(saved.isActive(), "active deve ser true");

            verify(validator).validateCreation(eq(loggedUserId), eq(entity), eq(true));
            verify(repository).save(eq(entity));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("save: não-admin -> valida criação, seta defaults defensivos e salva")
        void save_nonAdmin_setsDefaults_andSaves() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = new ChecklistTemplate();

            when(securityService.isAdmin()).thenReturn(false);
            when(repository.save(any(ChecklistTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            ChecklistTemplate saved = service.save(loggedUserId, entity);

            assertNotNull(saved);
            assertFalse(saved.isDefault(), "default deve ser false");
            assertTrue(saved.isActive(), "active deve ser true");

            verify(validator).validateCreation(eq(loggedUserId), eq(entity), eq(false));
            verify(repository).save(eq(entity));
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("findById: admin -> usa repository.findById")
        void findById_admin_usesFindById() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();
            ChecklistTemplate entity = new ChecklistTemplate();

            when(securityService.isAdmin()).thenReturn(true);
            when(repository.findById(id)).thenReturn(Optional.of(entity));

            ChecklistTemplate found = service.findById(loggedUserId, id);

            assertSame(entity, found);

            verify(repository).findById(id);
            verify(repository, never()).findOne(anySpec());
        }

        @Test
        @DisplayName("findById: admin -> not found lança ObjectNotFound")
        void findById_admin_notFound_throws() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();

            when(securityService.isAdmin()).thenReturn(true);
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFound.class, () -> service.findById(loggedUserId, id));

            verify(repository).findById(id);
            verify(repository, never()).findOne(anySpec());
        }

        @Test
        @DisplayName("findById: não-admin -> usa repository.findOne(spec)")
        void findById_nonAdmin_usesFindOneSpec() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();
            ChecklistTemplate entity = new ChecklistTemplate();

            when(securityService.isAdmin()).thenReturn(false);
            when(repository.findOne(anySpec())).thenReturn(Optional.of(entity));

            ChecklistTemplate found = service.findById(loggedUserId, id);

            assertSame(entity, found);

            verify(repository).findOne(anySpec());
            verify(repository, never()).findById(any());
        }

        @Test
        @DisplayName("findById: não-admin -> not found lança ObjectNotFound")
        void findById_nonAdmin_notFound_throws() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();

            when(securityService.isAdmin()).thenReturn(false);
            when(repository.findOne(anySpec())).thenReturn(Optional.empty());

            assertThrows(ObjectNotFound.class, () -> service.findById(loggedUserId, id));

            verify(repository).findOne(anySpec());
            verify(repository, never()).findById(any());
        }
    }

    @Nested
    class Search {

        @Test
        @DisplayName("search: não-admin -> valida search, chama repository.findAll(spec,pageable) e mapeia DTO")
        void search_nonAdmin_validates_andMaps() {
            UUID loggedUserId = UUID.randomUUID();
            UUID userFilter = UUID.randomUUID(); // não-admin: service usa loggedUserId no spec base
            UUID ruleFilter = UUID.randomUUID();
            String title = "abc";
            String description = "def";
            Boolean active = true;
            int page = 0;
            int pageSize = 10;

            when(securityService.isAdmin()).thenReturn(false);

            ChecklistTemplate e1 = new ChecklistTemplate();
            AnswerChecklistTemplateDTO d1 = mock(AnswerChecklistTemplateDTO.class);

            Page<ChecklistTemplate> entities =
                    new PageImpl<>(java.util.List.of(e1), PageRequest.of(page, pageSize), 1);

            when(repository.findAll(ArgumentMatchers.<Specification<ChecklistTemplate>>any(), any(Pageable.class)))
                    .thenReturn(entities);
            when(mapper.toDTO(e1)).thenReturn(d1);

            Page<AnswerChecklistTemplateDTO> out = service.search(
                    loggedUserId, userFilter, ruleFilter, title, description, active, page, pageSize
            );

            assertNotNull(out);
            assertEquals(1, out.getContent().size());
            assertSame(d1, out.getContent().get(0));
            assertEquals(1, out.getTotalElements()); // aqui faz sentido: page=0 e total=1

            verify(validator).validateSearch(eq(loggedUserId), eq(userFilter), eq(false), eq(page), eq(pageSize));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(ArgumentMatchers.<Specification<ChecklistTemplate>>any(), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(page, pageable.getPageNumber());
            assertEquals(pageSize, pageable.getPageSize());

            verify(mapper).toDTO(e1);
        }

        @Test
        @DisplayName("search: admin -> valida search, chama repository.findAll(spec,pageable) e mapeia DTO")
        void search_admin_validates_andMaps() {
            UUID loggedUserId = UUID.randomUUID();
            UUID userFilter = UUID.randomUUID();
            UUID ruleFilter = UUID.randomUUID();
            int page = 1;
            int pageSize = 20;

            when(securityService.isAdmin()).thenReturn(true);

            ChecklistTemplate e1 = new ChecklistTemplate();
            AnswerChecklistTemplateDTO d1 = mock(AnswerChecklistTemplateDTO.class);

            // Cenário realista: page=1 com pageSize=20 e total=21 => segunda página tem 1 item
            Page<ChecklistTemplate> entities =
                    new PageImpl<>(java.util.List.of(e1), PageRequest.of(page, pageSize), 21);

            when(repository.findAll(ArgumentMatchers.<Specification<ChecklistTemplate>>any(), any(Pageable.class)))
                    .thenReturn(entities);
            when(mapper.toDTO(e1)).thenReturn(d1);

            Page<AnswerChecklistTemplateDTO> out = service.search(
                    loggedUserId, userFilter, ruleFilter, null, null, null, page, pageSize
            );

            assertNotNull(out);
            assertEquals(1, out.getContent().size());
            assertSame(d1, out.getContent().get(0));
            assertEquals(21, out.getTotalElements()); // agora bate com o total simulado

            verify(validator).validateSearch(eq(loggedUserId), eq(userFilter), eq(true), eq(page), eq(pageSize));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(ArgumentMatchers.<Specification<ChecklistTemplate>>any(), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(page, pageable.getPageNumber());
            assertEquals(pageSize, pageable.getPageSize());

            verify(mapper).toDTO(e1);
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("update: valida, busca entidade, aplica mapper.updateFromDTO, salva e retorna DTO")
        void update_happyPath() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);

            when(securityService.isAdmin()).thenReturn(false);

            ChecklistTemplate existing = new ChecklistTemplate();
            ChecklistTemplate saved = new ChecklistTemplate();
            AnswerChecklistTemplateDTO outDto = mock(AnswerChecklistTemplateDTO.class);

            when(repository.findOne(anySpec())).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(saved);
            when(mapper.toDTO(saved)).thenReturn(outDto);

            AnswerChecklistTemplateDTO result = service.update(loggedUserId, id, dto);

            assertSame(outDto, result);

            verify(validator).validateUpdate(eq(loggedUserId), eq(id), eq(dto), eq(false));
            verify(repository).findOne(anySpec());
            verify(mapper).updateFromDTO(eq(dto), eq(existing));
            verify(repository).save(eq(existing));
            verify(mapper).toDTO(eq(saved));
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("delete: valida, busca entidade, seta active=false e salva (soft delete)")
        void delete_setsActiveFalse_andSaves() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();

            when(securityService.isAdmin()).thenReturn(false);

            ChecklistTemplate entity = spy(new ChecklistTemplate());
            entity.setActive(true);

            when(repository.findOne(anySpec())).thenReturn(Optional.of(entity));
            when(repository.save(entity)).thenReturn(entity);

            service.delete(loggedUserId, id);

            verify(validator).validateDelete(eq(loggedUserId), eq(id), eq(false));
            verify(entity).setActive(false);
            verify(repository).save(eq(entity));
        }

        @Test
        @DisplayName("delete: entidade não encontrada -> propaga ObjectNotFound e não salva")
        void delete_notFound_throws() {
            UUID loggedUserId = UUID.randomUUID();
            UUID id = UUID.randomUUID();

            when(securityService.isAdmin()).thenReturn(false);
            when(repository.findOne(anySpec())).thenReturn(Optional.empty());

            assertThrows(ObjectNotFound.class, () -> service.delete(loggedUserId, id));

            verify(validator).validateDelete(eq(loggedUserId), eq(id), eq(false));
            verify(repository).findOne(anySpec());
            verify(repository, never()).save(any());
        }
    }
}