package com.vectorlabs.mapper;

import com.vectorlabs.dto.checklisttemplate.AnswerChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.RegisterChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ChecklistTemplate;
import com.vectorlabs.model.Rule;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.repository.RuleRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ChecklistTemplateMapper {

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected RuleRepository ruleRepository;

    // =========================
    // TO DTO
    // =========================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ruleId", source = "rule.id")
    public abstract AnswerChecklistTemplateDTO toDTO(ChecklistTemplate entity);

    // =========================
    // REGISTER
    // =========================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "rule", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract ChecklistTemplate fromRegisterDTO(RegisterChecklistTemplateDTO dto);

    @AfterMapping
    protected void afterRegister(RegisterChecklistTemplateDTO dto,
                                 @MappingTarget ChecklistTemplate entity) {

        AppUser user = appUserRepository.findById(dto.userId())
                .orElseThrow(() -> new ObjectNotFound("AppUser not found"));

        Rule rule = ruleRepository.findById(dto.ruleId())
                .orElseThrow(() -> new ObjectNotFound("Rule not found"));

        entity.setUser(user);
        entity.setRule(rule);

        entity.setActive(true);

        if (dto.isDefault() != null) {
            entity.setDefault(dto.isDefault());
        } else {
            entity.setDefault(false);
        }
    }

    // =========================
    // UPDATE (PATCH)
    // =========================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "rule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    public abstract void updateFromDTO(UpdateChecklistTemplateDTO dto,
                                       @MappingTarget ChecklistTemplate entity);
}