package com.vectorlabs.mapper;

import com.vectorlabs.dto.rule.ruleitem.AnswerRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.RegisterRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.repository.RuleItemRepository;
import com.vectorlabs.repository.RuleModuleRepository;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class RuleItemMapper {

    @Autowired
    protected RuleModuleRepository moduleRepository;

    @Autowired
    protected RuleItemRepository ruleItemRepository;

    // --------- CREATE ---------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", source = "moduleId", qualifiedByName = "mapModule")
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "mapParent")
    @Mapping(target = "active", constant = "true")
    public abstract RuleItem fromRegisterDTO(RegisterRuleItemDTO dto);

    // --------- UPDATE (PATCH) ---------

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true) // immutable
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "mapParent")
    public abstract void updateFromDTO(UpdateRuleItemDTO dto, @MappingTarget RuleItem entity);

    // --------- ANSWER ---------

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "moduleCode", source = "module.moduleCode")
    @Mapping(target = "parentId", source = "parent.id")
    public abstract AnswerRuleItemDTO toDTO(RuleItem entity);

    // --------- HELPERS ---------

    @Named("mapModule")
    protected RuleModule mapModule(UUID id) {
        if (id == null) return null;
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleModule not found: " + id));
    }

    @Named("mapParent")
    protected RuleItem mapParent(UUID id) {
        if (id == null) return null;
        return ruleItemRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Parent RuleItem not found: " + id));
    }
}
