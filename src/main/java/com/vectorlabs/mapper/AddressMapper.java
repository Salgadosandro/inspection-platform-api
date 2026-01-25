package com.vectorlabs.mapper;

import com.vectorlabs.dto.address.AnswerAddressDTO;
import com.vectorlabs.dto.address.RegisterAddressDTO;
import com.vectorlabs.dto.address.UpdateAddressDTO;
import com.vectorlabs.model.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    // CREATE
    Address toEntity(RegisterAddressDTO dto);

    // READ
    AnswerAddressDTO toAnswerDTO(Address entity);

    // UPDATE/PATCH (ignora campos null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateAddressDTO dto, @MappingTarget Address entity);
}
