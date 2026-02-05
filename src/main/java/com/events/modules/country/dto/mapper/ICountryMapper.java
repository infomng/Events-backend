package com.events.modules.country.dto.mapper;

import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;
import com.events.modules.country.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ICountryMapper {

    /**
     * Convert Country entity to CountryDto.
     */
    CountryDto toDto(Country country);

    /**
     * Convert list of Country entities to list of CountryDtos.
     */
    List<CountryDto> toDtoList(List<Country> countries);

    /**
     * Convert CountryCreateCommandDto to Country entity.
     * The name and code will be populated from the PaysEnum.
     */
    @Mapping(target = "name", source = "paysEnum.name")
    @Mapping(target = "code", source = "paysEnum.code")
    Country toEntity(CountryCreateCommandDto dto);

    Country toEntity(CountryDto dto);

    /**
     * Update existing Country entity from CountryUpdateCommandDto.
     * The name and code will be updated from the PaysEnum.
     */
    @Mapping(target = "name", source = "paysEnum.name")
    @Mapping(target = "code", source = "paysEnum.code")
    void updateEntityFromDto(CountryUpdateCommandDto dto, @MappingTarget Country country);
}
