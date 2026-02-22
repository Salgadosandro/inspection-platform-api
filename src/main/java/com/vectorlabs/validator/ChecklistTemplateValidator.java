package com.vectorlabs.validator;

import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.ChecklistTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChecklistTemplateValidator {

    private static final int TITLE_MAX = 200;
    private static final int DESCRIPTION_MAX = 2000;

    // =========================
    // CREATE
    // =========================
    public void validateCreation(UUID loggedUserId, ChecklistTemplate entity, boolean isAdmin) {

        if (loggedUserId == null) {
            throw new MissingRequiredFieldException("Logged user id cannot be null.");
        }
        if (entity == null) {
            throw new MissingRequiredFieldException("ChecklistTemplate entity cannot be null.");
        }

        if (entity.getUser() == null || entity.getUser().getId() == null) {
            throw new MissingRequiredFieldException("Field 'user' is required.");
        }
        if (entity.getRule() == null || entity.getRule().getId() == null) {
            throw new MissingRequiredFieldException("Field 'rule' is required.");
        }

        if (isBlank(entity.getTitle())) {
            throw new InvalidFieldException("Field 'title' is required.");
        }
        if (entity.getTitle().trim().length() > TITLE_MAX) {
            throw new InvalidFieldException("Field 'title' exceeds max length of " + TITLE_MAX + ".");
        }

        if (entity.getDescription() != null &&
                entity.getDescription().trim().length() > DESCRIPTION_MAX) {
            throw new InvalidFieldException("Field 'description' exceeds max length of " + DESCRIPTION_MAX + ".");
        }

        // ownership: user comum não pode criar para outro user
        if (!isAdmin && !entity.getUser().getId().equals(loggedUserId)) {
            throw new AccessDeniedException("You cannot create checklist for another user.");
        }
    }

    // =========================
    // UPDATE
    // =========================
    public void validateUpdate(UUID loggedUserId, UUID id, UpdateChecklistTemplateDTO dto, boolean isAdmin) {

        if (loggedUserId == null) {
            throw new MissingRequiredFieldException("Logged user id cannot be null.");
        }
        if (id == null) {
            throw new MissingRequiredFieldException("ChecklistTemplate id cannot be null.");
        }
        if (dto == null) {
            throw new MissingRequiredFieldException("UpdateChecklistTemplateDTO cannot be null.");
        }

        if (dto.title() != null && dto.title().trim().length() > TITLE_MAX) {
            throw new InvalidFieldException("Field 'title' exceeds max length of " + TITLE_MAX + ".");
        }
        if (dto.description() != null && dto.description().trim().length() > DESCRIPTION_MAX) {
            throw new InvalidFieldException("Field 'description' exceeds max length of " + DESCRIPTION_MAX + ".");
        }

        // ownership é garantido por findById no service (admin bypass)
        // aqui só valida entrada/forma.
    }

    // =========================
    // DELETE
    // =========================
    public void validateDelete(UUID loggedUserId, UUID id, boolean isAdmin) {

        if (loggedUserId == null) {
            throw new MissingRequiredFieldException("Logged user id cannot be null.");
        }
        if (id == null) {
            throw new MissingRequiredFieldException("ChecklistTemplate id cannot be null.");
        }
    }

    // =========================
    // SEARCH
    // =========================
    public void validateSearch(UUID loggedUserId, UUID userFilter, boolean isAdmin, Integer page, Integer pageSize) {

        if (loggedUserId == null) {
            throw new MissingRequiredFieldException("Logged user id cannot be null.");
        }
        if (page == null || page < 0) {
            throw new InvalidFieldException("Parameter 'page' must be >= 0.");
        }
        if (pageSize == null || pageSize < 1) {
            throw new InvalidFieldException("Parameter 'page_size' must be >= 1.");
        }

        // user comum não pode filtrar por outro userId
        if (!isAdmin && userFilter != null && !userFilter.equals(loggedUserId)) {
            throw new AccessDeniedException("You cannot filter checklist templates for another user.");
        }
    }

    // =========================
    // Helpers
    // =========================
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}