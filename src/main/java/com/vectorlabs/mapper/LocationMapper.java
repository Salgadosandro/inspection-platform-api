package com.vectorlabs.mapper;

import com.vectorlabs.dto.location.AnswerLocationDTO;
import com.vectorlabs.dto.location.RegisterLocationDTO;
import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.Address;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.model.Location;
import com.vectorlabs.repository.ClientCompanyRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {ClientCompanyMapper.class})
public abstract class LocationMapper {

    @Autowired
    protected ClientCompanyRepository clientCompanyRepository;

    // =======================
    // ENTITY -> ANSWER DTO
    // =======================
    @Mapping(source = "company", target = "clientCompany")
    @Mapping(source = "company.corporateName", target = "clientCompanyName")
    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.number", target = "number")
    @Mapping(source = "address.district", target = "neighborhood") // DTO usa neighborhood
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.zipCode", target = "zipCode")
    @Mapping(source = "address.country", target = "country")
    public abstract AnswerLocationDTO toDTO(Location entity);

    // =======================
    // REGISTER DTO -> ENTITY
    // =======================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", source = "clientCompanyId")
    @Mapping(target = "address", expression = "java(buildAddress(dto))")
    public abstract Location fromRegisterDTO(RegisterLocationDTO dto);

    // =======================
    // UPDATE DTO -> ENTITY (PATCH)
    // =======================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "address", ignore = true) // atualizamos via @AfterMapping para garantir address != null
    public abstract void updateFromDTO(UpdateLocationDTO dto, @MappingTarget Location entity);

    @AfterMapping
    protected void patchAddress(UpdateLocationDTO dto, @MappingTarget Location entity) {
        if (dto == null) return;

        if (entity.getAddress() == null) {
            entity.setAddress(new Address());
        }

        Address a = entity.getAddress();

        if (dto.street() != null) a.setStreet(dto.street());
        if (dto.number() != null) a.setNumber(dto.number());
        if (dto.neighborhood() != null) a.setDistrict(dto.neighborhood()); // DTO neighborhood -> Address district
        if (dto.city() != null) a.setCity(dto.city());
        if (dto.state() != null) a.setState(dto.state());
        if (dto.zipCode() != null) a.setZipCode(dto.zipCode());
        if (dto.country() != null) a.setCountry(dto.country());
    }

    // =======================
    // HELPERS (ID -> ENTITY)
    // =======================
    protected ClientCompany mapClientCompany(UUID id) {
        if (id == null) return null;
        return clientCompanyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("ClientCompany not found with id: " + id));
    }

    protected Address buildAddress(RegisterLocationDTO dto) {
        Address address = new Address();
        address.setStreet(dto.street());
        address.setNumber(dto.number());
        address.setDistrict(dto.neighborhood()); // DTO neighborhood -> Address district
        address.setCity(dto.city());
        address.setState(dto.state());
        address.setZipCode(dto.zipCode());
        address.setCountry(dto.country());
        return address;
    }
}
