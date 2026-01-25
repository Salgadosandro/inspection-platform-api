package com.vectorlabs.mapper;

import com.vectorlabs.dto.appuser.AnswerAppUserDTO;
import com.vectorlabs.dto.appuser.RegisterAppUserDTO;
import com.vectorlabs.dto.appuser.UpdateAppUserDTO;

import com.vectorlabs.model.AppUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface AppUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "providerUserId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "cnpj", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    AppUser toEntity(RegisterAppUserDTO dto);

    // READ
    AnswerAppUserDTO toAnswerDTO(AppUser entity);

    // UPDATE/PATCH (ignora null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(UpdateAppUserDTO dto, @MappingTarget AppUser entity);
}
