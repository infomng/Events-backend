package com.events.modules.event.dto.mapper;

import com.events.modules.event.dto.CreatePriceCategoryDto;
import com.events.modules.event.entity.aggregate.PriceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IPriceCategoryMapper {

    CreatePriceCategoryDto toDto(PriceCategory priceCategory);

    List<CreatePriceCategoryDto> toDtoList(List<PriceCategory> priceCategories);

    @Mapping(target = "event", ignore = true)
    @Mapping(target = "availableTickets", source = "totalTickets")
    PriceCategory toEntity(CreatePriceCategoryDto createPriceCategoryDto);

    List<PriceCategory> toEntityList(List<CreatePriceCategoryDto> createPriceCategoryDtos);
}
