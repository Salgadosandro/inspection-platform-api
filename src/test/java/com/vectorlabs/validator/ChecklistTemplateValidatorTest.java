package com.vectorlabs.validator;

import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.ChecklistTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChecklistTemplateValidator (pure unit, no Spring context).
 */
class ChecklistTemplateValidatorTest {

    private ChecklistTemplateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChecklistTemplateValidator();
    }

    // =========================
    // CREATE
    // =========================
    @Nested
    class ValidateCreation {

        @Test
        @DisplayName("validateCreation: loggedUserId null -> MissingRequiredFieldException")
        void validateCreation_loggedUserIdNull_throws() {
            ChecklistTemplate entity = mock(ChecklistTemplate.class);
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(null, entity, true));
        }

        @Test
        @DisplayName("validateCreation: entity null -> MissingRequiredFieldException")
        void validateCreation_entityNull_throws() {
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(UUID.randomUUID(), null, true));
        }

        @Test
        @DisplayName("validateCreation: user null -> MissingRequiredFieldException")
        void validateCreation_userNull_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            when(entity.getUser()).thenReturn(null);

            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: user.id null -> MissingRequiredFieldException")
        void validateCreation_userIdNull_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(null);
            when(entity.getUser()).thenReturn(user);

            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: rule null -> MissingRequiredFieldException")
        void validateCreation_ruleNull_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            when(entity.getRule()).thenReturn(null);

            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: rule.id null -> MissingRequiredFieldException")
        void validateCreation_ruleIdNull_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(null);
            when(entity.getRule()).thenReturn(rule);

            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: title blank -> InvalidFieldException")
        void validateCreation_titleBlank_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            when(entity.getTitle()).thenReturn("   ");

            assertThrows(InvalidFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: title > 200 -> InvalidFieldException")
        void validateCreation_titleTooLong_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            String longTitle = "a".repeat(201);
            when(entity.getTitle()).thenReturn(longTitle);

            assertThrows(InvalidFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: description > 2000 -> InvalidFieldException")
        void validateCreation_descriptionTooLong_throws() {
            UUID loggedUserId = UUID.randomUUID();
            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            when(entity.getTitle()).thenReturn("OK");
            String longDesc = "a".repeat(2001);
            when(entity.getDescription()).thenReturn(longDesc);

            assertThrows(InvalidFieldException.class,
                    () -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: não-admin tentando criar para outro user -> AccessDeniedException")
        void validateCreation_nonAdmin_creatingForAnotherUser_throws() {
            UUID loggedUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(otherUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            when(entity.getTitle()).thenReturn("OK");
            when(entity.getDescription()).thenReturn(null);

            assertThrows(AccessDeniedException.class,
                    () -> validator.validateCreation(loggedUserId, entity, false));
        }

        @Test
        @DisplayName("validateCreation: admin pode criar para outro user (passa)")
        void validateCreation_admin_canCreateForAnotherUser_passes() {
            UUID loggedUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(otherUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            when(entity.getTitle()).thenReturn("Título OK");
            when(entity.getDescription()).thenReturn("Descrição OK");

            assertDoesNotThrow(() -> validator.validateCreation(loggedUserId, entity, true));
        }

        @Test
        @DisplayName("validateCreation: não-admin criando para si mesmo (passa)")
        void validateCreation_nonAdmin_creatingForSelf_passes() {
            UUID loggedUserId = UUID.randomUUID();

            ChecklistTemplate entity = mock(ChecklistTemplate.class);

            var user = mock(com.vectorlabs.model.AppUser.class);
            when(user.getId()).thenReturn(loggedUserId);
            when(entity.getUser()).thenReturn(user);

            var rule = mock(com.vectorlabs.model.Rule.class);
            when(rule.getId()).thenReturn(UUID.randomUUID());
            when(entity.getRule()).thenReturn(rule);

            when(entity.getTitle()).thenReturn("OK");
            when(entity.getDescription()).thenReturn(null);

            assertDoesNotThrow(() -> validator.validateCreation(loggedUserId, entity, false));
        }
    }

    // =========================
    // UPDATE
    // =========================
    @Nested
    class ValidateUpdate {

        @Test
        @DisplayName("validateUpdate: loggedUserId null -> MissingRequiredFieldException")
        void validateUpdate_loggedUserIdNull_throws() {
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateUpdate(null, UUID.randomUUID(), dto, true));
        }

        @Test
        @DisplayName("validateUpdate: id null -> MissingRequiredFieldException")
        void validateUpdate_idNull_throws() {
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateUpdate(UUID.randomUUID(), null, dto, true));
        }

        @Test
        @DisplayName("validateUpdate: dto null -> MissingRequiredFieldException")
        void validateUpdate_dtoNull_throws() {
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateUpdate(UUID.randomUUID(), UUID.randomUUID(), null, true));
        }

        @Test
        @DisplayName("validateUpdate: dto.title > 200 -> InvalidFieldException")
        void validateUpdate_titleTooLong_throws() {
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);
            when(dto.title()).thenReturn("a".repeat(201));
            when(dto.description()).thenReturn(null);

            assertThrows(InvalidFieldException.class,
                    () -> validator.validateUpdate(UUID.randomUUID(), UUID.randomUUID(), dto, true));
        }

        @Test
        @DisplayName("validateUpdate: dto.description > 2000 -> InvalidFieldException")
        void validateUpdate_descriptionTooLong_throws() {
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);
            when(dto.title()).thenReturn(null);
            when(dto.description()).thenReturn("a".repeat(2001));

            assertThrows(InvalidFieldException.class,
                    () -> validator.validateUpdate(UUID.randomUUID(), UUID.randomUUID(), dto, true));
        }

        @Test
        @DisplayName("validateUpdate: entrada válida (passa)")
        void validateUpdate_valid_passes() {
            UpdateChecklistTemplateDTO dto = mock(UpdateChecklistTemplateDTO.class);
            when(dto.title()).thenReturn("OK");
            when(dto.description()).thenReturn("OK");

            assertDoesNotThrow(() -> validator.validateUpdate(UUID.randomUUID(), UUID.randomUUID(), dto, false));
        }
    }

    // =========================
    // DELETE
    // =========================
    @Nested
    class ValidateDelete {

        @Test
        @DisplayName("validateDelete: loggedUserId null -> MissingRequiredFieldException")
        void validateDelete_loggedUserIdNull_throws() {
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateDelete(null, UUID.randomUUID(), true));
        }

        @Test
        @DisplayName("validateDelete: id null -> MissingRequiredFieldException")
        void validateDelete_idNull_throws() {
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateDelete(UUID.randomUUID(), null, true));
        }

        @Test
        @DisplayName("validateDelete: válido (passa)")
        void validateDelete_valid_passes() {
            assertDoesNotThrow(() -> validator.validateDelete(UUID.randomUUID(), UUID.randomUUID(), false));
        }
    }

    // =========================
    // SEARCH
    // =========================
    @Nested
    class ValidateSearch {

        @Test
        @DisplayName("validateSearch: loggedUserId null -> MissingRequiredFieldException")
        void validateSearch_loggedUserIdNull_throws() {
            assertThrows(MissingRequiredFieldException.class,
                    () -> validator.validateSearch(null, null, true, 0, 10));
        }

        @Test
        @DisplayName("validateSearch: page null -> InvalidFieldException")
        void validateSearch_pageNull_throws() {
            assertThrows(InvalidFieldException.class,
                    () -> validator.validateSearch(UUID.randomUUID(), null, true, null, 10));
        }

        @Test
        @DisplayName("validateSearch: page < 0 -> InvalidFieldException")
        void validateSearch_pageNegative_throws() {
            assertThrows(InvalidFieldException.class,
                    () -> validator.validateSearch(UUID.randomUUID(), null, true, -1, 10));
        }

        @Test
        @DisplayName("validateSearch: pageSize null -> InvalidFieldException")
        void validateSearch_pageSizeNull_throws() {
            assertThrows(InvalidFieldException.class,
                    () -> validator.validateSearch(UUID.randomUUID(), null, true, 0, null));
        }

        @Test
        @DisplayName("validateSearch: pageSize < 1 -> InvalidFieldException")
        void validateSearch_pageSizeTooSmall_throws() {
            assertThrows(InvalidFieldException.class,
                    () -> validator.validateSearch(UUID.randomUUID(), null, true, 0, 0));
        }

        @Test
        @DisplayName("validateSearch: não-admin com userFilter diferente do loggedUserId -> AccessDeniedException")
        void validateSearch_nonAdmin_filterAnotherUser_throws() {
            UUID loggedUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            assertThrows(AccessDeniedException.class,
                    () -> validator.validateSearch(loggedUserId, otherUserId, false, 0, 10));
        }

        @Test
        @DisplayName("validateSearch: não-admin com userFilter = loggedUserId (passa)")
        void validateSearch_nonAdmin_filterSelf_passes() {
            UUID loggedUserId = UUID.randomUUID();
            assertDoesNotThrow(() -> validator.validateSearch(loggedUserId, loggedUserId, false, 0, 10));
        }

        @Test
        @DisplayName("validateSearch: não-admin com userFilter null (passa)")
        void validateSearch_nonAdmin_userFilterNull_passes() {
            UUID loggedUserId = UUID.randomUUID();
            assertDoesNotThrow(() -> validator.validateSearch(loggedUserId, null, false, 0, 10));
        }

        @Test
        @DisplayName("validateSearch: admin pode filtrar qualquer user (passa)")
        void validateSearch_admin_filterAnyUser_passes() {
            UUID loggedUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            assertDoesNotThrow(() -> validator.validateSearch(loggedUserId, otherUserId, true, 0, 10));
        }
    }
}